package net.perfectdreams.dreamjetpack

import com.okkero.skedule.schedule
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreambedrockintegrations.utils.isBedrockClient
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import net.perfectdreams.dreamcore.utils.preferences.sendMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class DreamJetpack : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamJetpack
		val PREFIX = "§8[§a§lJetpack§8]§e"
		const val TAKE_DAMAGE_EVERY = 180
		val IS_JETPACK_KEY = SparklyNamespacedBooleanKey("is_jetpack")
	}

	val flyingPlayers = mutableSetOf<Player>()
	val bossBars = mutableMapOf<Player, BossBar>()

	val jetpackType = Material.CHAINMAIL_CHESTPLATE
	var durabilityTicks = 0

	override fun softEnable() {
		super.softEnable()

		INSTANCE = this

		flyingPlayers.addAll(Bukkit.getOnlinePlayers().filter { player ->
			val chestplate = player.inventory.chestplate
			isJetpack(player, chestplate)
		})

		registerCommand(object: SparklyCommand(arrayOf("dreamjetpack"), permission = "dreamjetpack.setup") {
			@Subcommand
			fun root(sender: CommandSender) {
				reloadConfig()
				sender.sendMessage("§aReload concluído!")
			}

			@Subcommand(["give"])
			fun root(sender: Player) {
				sender.inventory.addItem(
					ItemStack(Material.CHAINMAIL_CHESTPLATE)
						.meta<ItemMeta> {
							displayNameWithoutDecorations {
								color(NamedTextColor.GOLD)
								decorate(TextDecoration.BOLD)
								content("Jetpack")
							}

							persistentDataContainer.set(IS_JETPACK_KEY, true)
						}

				)
				sender.sendMessage("§aVocê recebeu uma Jetpack!")
			}
		})

		scheduler().schedule(this) {
			while (true) {
				val toBeRemoved = mutableSetOf<Player>()

				for (player in flyingPlayers) {
					if (!player.isValid) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.allowFlight = false
						continue
					}

					val chestplate = player.inventory.chestplate
					val isJetpack = isJetpack(player, chestplate)

					if (!isJetpack || chestplate == null) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.sendMessage("$PREFIX §cVocê não está mais usando a Jetpack!")
						player.allowFlight = false
						continue
					}

					val blacklistedWorlds = config.getStringList("blacklisted-worlds")

					if (blacklistedWorlds.contains(player.world.name)) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.sendMessage("$PREFIX §cVocê não pode voar aqui")
						player.allowFlight = false
						continue
					}

					if (!player.isFlying)
						continue

					if (chestplate.containsEnchantment(Enchantment.MENDING))
						chestplate.removeEnchantment(Enchantment.MENDING)

					val meta = chestplate.itemMeta as org.bukkit.inventory.meta.Damageable

					val applyDamage = when {
						player.hasPermission("dreamjetpack.vip++") -> 0
						player.hasPermission("dreamjetpack.vip+") -> 1
						player.hasPermission("dreamjetpack.vip") -> 2
						else -> 3
					}

					if (durabilityTicks % TAKE_DAMAGE_EVERY == 0) {
						if (applyDamage == 0) {
							meta.damage = 0
						} else {
							meta.damage += applyDamage
						}
					}

					if (meta.damage > 240) {
						toBeRemoved.add(player)
						bossBars[player]?.removeAll()
						bossBars.remove(player)

						player.inventory.chestplate = ItemStack(Material.AIR)
						player.sendMessage("$PREFIX §cSua Jetpack está toda detonada e explodiu!")
						player.allowFlight = false
						continue
					}

					if (!bossBars.contains(player)) {
						val bossBar = Bukkit.createBossBar("...", BarColor.GREEN, BarStyle.SEGMENTED_20)
						bossBars[player] = bossBar
						bossBar.addPlayer(player)
					}

					val bossBar = bossBars[player]
					if (bossBar != null) {
						val percentageRemaining = (240 - meta.damage).toDouble() / 240
						bossBar.progress = percentageRemaining

						bossBar.color = when {
							player.hasPermission("dreamjetpack.vip++") -> BarColor.PINK
							percentageRemaining >= 0.75 -> BarColor.GREEN
							percentageRemaining >= 0.25 -> BarColor.YELLOW
							else -> BarColor.RED
						}

						val timeRemaining = if (applyDamage == 0)
							-1
						else ((TAKE_DAMAGE_EVERY * (240 - meta.damage) / applyDamage) - durabilityTicks % TAKE_DAMAGE_EVERY)

						val minutes = timeRemaining / 60
						val seconds = timeRemaining % 60

						if (player.isBedrockClient) {
							bossBar.setTitle(
								if (applyDamage == 0) {
									"§dWoosh! §aVocê pode voar por mais §e∞§a!"
								} else {
									"§dWoosh! §aVocê pode voar por mais §e$minutes minutos e $seconds segundos§a!"
								}
							)
						} else {
							bossBar.setTitle(
								if (applyDamage == 0) {
									"§dWoosh! §aVocê pode voar por mais §e∞§a!"
								} else {
									"§dWoosh! §aVocê pode voar por mais §e$minutes minutos e $seconds segundos§a!"
								}
							)
						}
					}

					player.world.spawnParticle(Particle.SMOKE, player.location, 20, 1.0, 1.0, 1.0)

					chestplate.itemMeta = meta as ItemMeta
				}

				flyingPlayers.removeAll(toBeRemoved)

				durabilityTicks++

				waitFor(20)
			}
		}

		registerEvents(this)
	}

	@EventHandler
	fun onShift(e: PlayerToggleSneakEvent) {
		val chestplate = e.player.inventory.chestplate
		if (e.player.isOnGround && e.isSneaking && chestplate?.type == jetpackType) {
			val isJetpack = isJetpack(e.player, chestplate)

			if (!isJetpack) {
				e.player.sendMessage("$PREFIX §cQue isso, comprou essa Jetpack no barzinho da esquina? Essa Jetpack é um risco a sua vida! Você apenas pode voar com jetpacks compradas na §6/loja§c!")
			} else {
				if (!e.player.allowFlight) {
					val blacklistedWorlds = config.getStringList("blacklisted-worlds")

					if (blacklistedWorlds.contains(e.player.world.name)) {
						e.player.sendMessage("$PREFIX §cVocê não pode voar aqui")
						return
					}

					e.player.allowFlight = true

					flyingPlayers.add(e.player)

					e.player.sendMessage("$PREFIX §eWoosh! Sua Jetpack foi ativada e está pronta para uso!", BroadcastType.JETPACK_MESSAGE)
				} else {
					e.player.allowFlight = false

					flyingPlayers.remove(e.player)
					bossBars[e.player]?.removeAll()
					bossBars.remove(e.player)

					e.player.sendMessage("$PREFIX §eSua Jetpack foi desativada!", BroadcastType.JETPACK_MESSAGE)
				}
			}
		}
	}

	fun isJetpack(player: Player, chestplate: ItemStack?): Boolean {
		if (chestplate == null)
			return false

		if (chestplate.hasItemMeta()) {
			val itemMeta = chestplate.itemMeta
			if (itemMeta.persistentDataContainer.get(IS_JETPACK_KEY))
				return true

			if (chestplate.itemMeta.displayName == "§6§lJetpack") {
				itemMeta.persistentDataContainer.set(IS_JETPACK_KEY, true)
				chestplate.itemMeta = itemMeta
				return true
			}
		}

		return false
	}
}