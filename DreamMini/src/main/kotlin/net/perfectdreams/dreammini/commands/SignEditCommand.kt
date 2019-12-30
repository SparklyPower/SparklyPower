package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player

class SignEditCommand(val m: DreamMini) : SparklyCommand(arrayOf("signedit", "editarplaca"), permission = "signedit.edit") {

	@Subcommand
	fun root(sender: Player) {
		sender.sendMessage(generateCommandInfo("signedit <linha> <novotexto>"))
	}

	@Subcommand
	fun signedit(sender: Player, arg: String, newText: Array<String>) {
		val string = arg.toIntOrNull()
		val targetBlock = sender.getTargetBlock(null as Set<Material>?, 10)
		val type = targetBlock?.type

		if (string == null || (string) !in 1..4) {
			sender.sendMessage("§cVocê não colocou uma linha válida! Linhas devem ser entre 1 e 4!")
			return
		}

		if (type == null || !type.name.contains("SIGN")) {
			sender.sendMessage("§cVocê precisa estar olhando para uma placa!")
			return
		}

		val sign = targetBlock.state as Sign
		val text = newText.toMutableList()

		sign.setLine(string - 1, text.joinToString(" ").translateColorCodes())
		sign.update()

		sender.sendMessage("§aLinha $string atualizada!")
	}
}