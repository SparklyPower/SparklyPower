package net.perfectdreams.dreamkits.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.*
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.commands.annotation.SubcommandPermission
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamkits.DreamKits
import net.perfectdreams.dreamkits.tables.Kits
import net.perfectdreams.dreamkits.utils.PlayerKitsInfo
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

class KitCommand(val m: DreamKits) : AbstractCommand("kits", listOf("kit")) {
	@Subcommand
	fun root(p0: CommandSender) {
		if (p0 !is Player)
			return

		val now = System.currentTimeMillis()

		m.launchMainThread {
			val kitsInfo = onAsyncThread {
				transaction {
					Kits.select { Kits.id eq p0.uniqueId }.firstOrNull()?.get(Kits.kitsInfo)
				} ?: PlayerKitsInfo()
			}

			val canUseKits = m.kits.filter { p0.hasPermission("dreamkits.kit.${it.name}") }
			val availableKits = canUseKits.filter {
				val lastUsed = kitsInfo.usedKits[it.name] ?: 0L

				now >= lastUsed + (it.delay * 1000)
			}
			val unavailableKits = canUseKits.filter { it !in availableKits }

			p0.sendMessage(
				textComponent {
					color(NamedTextColor.DARK_AQUA)
					append(DreamKits.PREFIX)
					append(" ")
					append("Kits disponíveis: ")

					var first = true

					availableKits.forEach {
						if (!first)
							append(", ")

						append(it.name) {
							color(NamedTextColor.AQUA)
							runCommandOnClick("/kit ${it.name}")
						}
						first = false
					}
				}
			)

			if (unavailableKits.isNotEmpty()) {
				p0.sendMessage(
					textComponent {
						color(NamedTextColor.RED)
						append(DreamKits.PREFIX)
						append(" ")
						append("Kits em espera: ")

						var first = true

						unavailableKits.forEach {
							if (!first)
								append(", ")

							append(it.name) {
								val lastUsed = kitsInfo.usedKits[it.name] ?: 0L

								color(NamedTextColor.AQUA)
								hoverEvent(
									HoverEvent.showText(
										textComponent {
											color(NamedTextColor.GRAY)
											append("Você precisa esperar ")
											append(DateUtils.formatDateDiff(lastUsed + (it.delay * 1000))) {
												color(NamedTextColor.GOLD)
											}
											append(" antes de poder resgatar este kit!")
										}
									)
								)
								runCommandOnClick("/kit ${it.name}")

							}
							first = false
						}
					}
				)
			}

			p0.sendMessage(
				textComponent {
					color(NamedTextColor.GRAY)
					append(DreamKits.PREFIX)
					append(" Para pegar um kit, use ")
					appendCommand("/kit NomeDoKit")
				}
			)
		}
	}

	@Subcommand(["reload"])
	@SubcommandPermission("dreamkits.setup")
	fun reload(p0: CommandSender) {
		m.loadKits()
		p0.sendMessage("§aRecarregado com sucesso!")
	}

	@Subcommand
	fun getKit(p0: Player, kitName: String) {
		val kit = m.kits.firstOrNull { it.name.equals(kitName, true) }

		if (kit == null) {
			p0.sendMessage(
				textComponent {
					color(NamedTextColor.RED)
					append(DreamKits.PREFIX)
					append(" ")
					append("Kit $kitName não existe! Para ver todos os kits, use ")
					runCommandOnClick("kits")
				}
			)
			return
		}

		if (!p0.hasPermission("dreamkits.kit.${kit.name}")) {
			p0.sendMessage(
				textComponent {
					color(NamedTextColor.RED)
					append(DreamKits.PREFIX)
					append(" ")
					append(LegacyComponentSerializer.legacySection().deserialize(withoutPermission!!))
				}
			)
			return
		}

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			val kitsInfo = transaction {
				Kits.select { Kits.id eq p0.uniqueId }.firstOrNull()?.get(Kits.kitsInfo)
			} ?: PlayerKitsInfo()

			val lastUsage = kitsInfo.usedKits.getOrDefault(kit.name, 0L)
			val diff = System.currentTimeMillis() - lastUsage

			switchContext(SynchronizationContext.SYNC)

			if (kit.delay * 1000 > diff && !p0.hasPermission("dreamkits.bypasstimer")) {
				val nextUse = lastUsage + (kit.delay * 1000)
				p0.sendMessage(
					textComponent {
						color(NamedTextColor.RED)
						append(DreamKits.PREFIX)
						append(" Você ainda precisa esperar ")
						append(DateUtils.formatDateDiff(nextUse)) {
							color(NamedTextColor.LIGHT_PURPLE)
						}
						append(" antes de poder pegar este kit...")
					}
				)
				return@schedule
			}

			kitsInfo.usedKits[kit.name] = System.currentTimeMillis()

			switchContext(SynchronizationContext.ASYNC)

			transaction {
				Kits.upsert(Kits.id) {
					it[id] = p0.uniqueId
					it[this.kitsInfo] = kitsInfo
				}
			}

			switchContext(SynchronizationContext.SYNC)

			m.giveKit(p0, kit)

			p0.sendMessage(
				textComponent {
					color(NamedTextColor.GREEN)
					append(DreamKits.PREFIX)
					append(" Você recebeu o kit ")
					append(kit.fancyName) {
						color(NamedTextColor.AQUA)
					}
					append("!")
				}
			)

			switchContext(SynchronizationContext.ASYNC)

			Webhooks.PANTUFA_INFO?.send("**${p0.name}** recebeu kit `${kit.name}`.")
		}
	}
}