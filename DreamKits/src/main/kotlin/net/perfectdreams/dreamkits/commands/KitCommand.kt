package net.perfectdreams.dreamkits.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.commands.annotation.SubcommandPermission
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamcore.utils.exposed.upsert
import net.perfectdreams.dreamkits.DreamKits
import net.perfectdreams.dreamkits.tables.Kits
import net.perfectdreams.dreamkits.utils.PlayerKitsInfo
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class KitCommand(val m: DreamKits) : AbstractCommand("kits", listOf("kit")) {
	@Subcommand
	fun root(p0: CommandSender) {
		p0.sendMessage(DreamKits.PREFIX + "§3Kits: " + m.kits.filter { p0.hasPermission("dreamkits.kit.${it.name}") }.joinToString(separator = "§3, ", transform = { "§9" + it.name }))
		p0.sendMessage(DreamKits.PREFIX + "§7Para pegar um kit, use §6/kit NomeDoKit")
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
			p0.sendMessage(DreamKits.PREFIX + "§cKit $kitName não existe! Para ver todos os kits, use §e/kits")
			return
		}

		if (!p0.hasPermission("dreamkits.kit.${kit.name}")) {
			p0.sendMessage(DreamKits.PREFIX + withoutPermission)
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
				val nextUse = diff + System.currentTimeMillis()
				p0.sendMessage(DreamKits.PREFIX + "§cVocê ainda precisa esperar §d${DateUtils.formatDateDiff(nextUse)}§c antes de poder pegar este kit...")
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

			p0.sendMessage(DreamKits.PREFIX + "§aVocê recebeu o kit §9${kit.fancyName}§a!")

			switchContext(SynchronizationContext.ASYNC)

			Webhooks.PANTUFA_INFO?.send(DiscordMessage(
					content = "**${p0.name}** recebeu kit `${kit.name}`."
			))
		}
	}
}