package net.perfectdreams.dreamlobbyfun.utils

import kotlinx.coroutines.Job
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.displays.DisplayBlock
import net.perfectdreams.dreamcore.utils.displays.SparklyDisplay
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.Color
import kotlin.math.cos

class ServerCitizen(
	val data: ServerCitizenData,
	val sparklyNPC: SparklyNPC,
	val m: DreamLobbyFun
) {
	var playerCountHologram: SparklyDisplayAndTextDisplay? = null
	var serverNameHologram: SparklyDisplayAndTextDisplay? = null
	var clickHereHologram: SparklyDisplayAndTextDisplay? = null

	var easeTask: Job? = null
	var animationTicks = 0

	fun update() {
		val entity = sparklyNPC.getEntity()

		if (entity == null) { // Se é null, quer dizer que o NPC ainda não nasceu
			m.logger.warning { "NPC ${sparklyNPC.uniqueId} ainda não nasceu!" }
			return
		}

		val holoLocation = sparklyNPC.initialLocation.clone().add(0.0, 3.1, 0.0)

		// We *could* use the same SparklyDisplay for each of these, but because we change the position of the "Click Here!" hologram, we won't xd
		if (playerCountHologram == null) {
			val sparklyDisplay = DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(m, holoLocation.clone().add(0.0, -0.285, 0.0))
			val textDisplayBlock = sparklyDisplay.addDisplayBlock()
			textDisplayBlock.text(
				textComponent {
					color(NamedTextColor.GRAY)
					content("??? players online")
				}
			)
			playerCountHologram = SparklyDisplayAndTextDisplay(sparklyDisplay, textDisplayBlock)
		}

		if (serverNameHologram == null) {
			val sparklyDisplay = DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(m, holoLocation)
			val textDisplayBlock = sparklyDisplay.addDisplayBlock()
			textDisplayBlock.text(
				textComponent {
					color(NamedTextColor.GREEN)
					decorate(TextDecoration.BOLD)
					append(LegacyComponentSerializer.legacySection().deserialize(data.fancyServerName.translateColorCodes()))
				}
			)
			serverNameHologram = SparklyDisplayAndTextDisplay(sparklyDisplay, textDisplayBlock)
		}

		if (clickHereHologram == null) {
			val sparklyDisplay = DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(m, holoLocation.clone().add(0.0, 0.5, 0.0))
			val textDisplayBlock = sparklyDisplay.addDisplayBlock()
			textDisplayBlock.text(
				textComponent {
					appendTextComponent {
						color(NamedTextColor.GOLD)
						decorate(TextDecoration.BOLD)
						content("»")
					}

					appendSpace()

					appendTextComponent {
						color(NamedTextColor.GREEN)
						decorate(TextDecoration.BOLD)
						content("CLIQUE AQUI")
					}

					appendSpace()

					appendTextComponent {
						color(NamedTextColor.GOLD)
						decorate(TextDecoration.BOLD)
						content("«")
					}
				}
			)
			textDisplayBlock.isShadowed = true
			textDisplayBlock.backgroundColor = Color.fromARGB(0, 0, 0, 0)
			clickHereHologram = SparklyDisplayAndTextDisplay(sparklyDisplay, textDisplayBlock)
		}

		val playerCountHologram = playerCountHologram!!
		val serverNameHologram = serverNameHologram!!
		val clickHereHologram = clickHereHologram!!

		val middle = holoLocation.clone().add(0.0, 0.5, 0.0)

		if (easeTask == null) {
			easeTask = m.launchMainThread {
				while (true) {
					val newLocation = middle.clone()
					val mod = animationTicks % 32

					clickHereHologram.sparklyDisplay.locationReference = LocationReference.fromBukkit(
						newLocation.add(
							0.0,
							if (mod > 16) {
								-0.285 + easeInOutSine((mod - 16) / 16.0) * 0.285
							} else {
								easeInOutSine(mod / 16.0) * -0.285
							},
							0.0
						)
					)
					clickHereHologram.sparklyDisplay.synchronizeBlocks()

					animationTicks++
					delayTicks(2)
				}
			}
		}

		val playerCount = DreamLobbyFun.SERVER_ONLINE_COUNT[data.serverName]

		if (playerCount == null) {
			playerCountHologram.textDisplayBlock.text(
				textComponent {
					color(NamedTextColor.GRAY)
					content("??? players online")
				}
			)
		} else {
			val singular = playerCount == 1

			if (singular) {
				playerCountHologram.textDisplayBlock.text(
					textComponent {
						color(NamedTextColor.GRAY)
						content("$playerCount player online")
					}
				)
			} else {
				playerCountHologram.textDisplayBlock.text(
					textComponent {
						color(NamedTextColor.GRAY)
						content("$playerCount player online")
					}
				)
			}
		}
	}

	fun easeInOutSine(x: Double): Double {
		return -(cos(Math.PI * x) - 1) / 2
	}

	data class SparklyDisplayAndTextDisplay(
		val sparklyDisplay: SparklyDisplay,
		val textDisplayBlock: DisplayBlock.TextDisplayBlock
	)
}