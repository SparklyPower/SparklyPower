package net.perfectdreams.dreamchat.utils.bot.responses

import com.okkero.skedule.schedule
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamcore.utils.extensions.canPlaceAt
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

class LorittaAssinaResponse : RegExResponse() {
	init {
		patterns.add("loritta|lori".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("assina".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("minha|meu".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("casa|prédio|apartamento|home|castelo".toPattern(Pattern.CASE_INSENSITIVE))
	}

	override fun getResponse(message: String, event: AsyncPlayerChatEvent): String? {
		val player = event.player

		if (hasSign(player)) {
			// Verificar se o player pode construir no target
			scheduler().schedule(DreamChat.INSTANCE) {
				val targetBlock = player.getTargetBlock(null as Set<Material>?, 10)

				var signBlock: Block? = null
				if (targetBlock.type != Material.AIR) { // Olhando para um bloco sólido...
					if (targetBlock.getRelative(BlockFace.UP).type == Material.AIR)	{ // E o bloco acima é ar!
						signBlock = targetBlock.getRelative(BlockFace.UP)
						if (!event.player.canPlaceAt(signBlock.location, Material.OAK_SIGN))  {
							ChatUtils.sendResponseAsLoritta(player, "§b${player.displayName}§a, eu não consegui colocar uma placa aonde você está...")
							return@schedule
						}

						signBlock.type = Material.OAK_SIGN
						val face = LocationUtils.yawToFace((player.location.yaw + 90) % 360, true).oppositeFace
						val blockData = signBlock.blockData as org.bukkit.block.data.type.Sign
						blockData.rotation = face
						signBlock.blockData = blockData
					} else { // Se o de cima não for ar, então o usuário quer assinar em uma parede!
						val face = LocationUtils.yawToFace((player.location.yaw + 90) % 360, true).oppositeFace
						val emptySpace = targetBlock.getRelative(face)
						if (!event.player.canPlaceAt(emptySpace.location, Material.OAK_SIGN))  {
							ChatUtils.sendResponseAsLoritta(player, "§b${player.displayName}§a, eu não consegui colocar uma placa aonde você está...")
							return@schedule
						}

						signBlock = BlockUtils.attachWallSignAt(emptySpace.location)
					}
				}

				if (signBlock == null) {
					ChatUtils.sendResponseAsLoritta(player, "§b${player.displayName}§a, eu não consegui colocar uma placa aonde você está...")
					return@schedule
				}

				val sign = signBlock.state as Sign
				sign.setLine(0, "§3§m---------")
				sign.setLine(1, "§6✪§3Loritta§6✪")
				sign.setLine(2, "§4aprova! (◕‿◕✿)")
				sign.setLine(3, "§3§m---------")
				sign.update()
				removeAnySign(player)
				ChatUtils.sendResponseAsLoritta(player, "§b${player.displayName}§a, prontinho! §d(◕‿◕✿)")
			}
			return null
		} else {
			ChatUtils.sendResponseAsLoritta(player, "§b${player.displayName}§a, bem... eu quero muuuito assinar para você, mas você tem que ter uma placa no seu inventário!")
		}
		return null
	}

	fun hasSign(player: Player): Boolean {
		player.inventory.forEach {
			if (it != null && it.type == Material.OAK_SIGN) {
				return true
			}
		}
		return false
	}

	fun removeAnySign(player: Player) {
		player.inventory.forEach {
			if (it != null && it.type == Material.OAK_SIGN) {
				it.amount -= 1
				return
			}
		}
	}
}