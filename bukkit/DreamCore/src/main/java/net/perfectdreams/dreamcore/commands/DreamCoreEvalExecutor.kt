package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.stripColorCode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta

class DreamCoreEvalExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamCoreEvalExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val heldItem = player.inventory.itemInMainHand

        if (heldItem.type != Material.WRITABLE_BOOK && heldItem.type != Material.WRITTEN_BOOK)
            context.fail("§cVocê precisa estar segurando um livro!")

        val bookMeta = heldItem.itemMeta as BookMeta
        val lines = bookMeta.pages.map { it.stripColorCode() }.joinToString("\n")

        player.sendMessage("§dExecutando...")
        player.sendMessage(lines)

        val content = """
			${Imports.IMPORTS}

			class EvaluatedCode {
				fun doStuff(player: Player) {
					${lines}
				}
			}

			EvaluatedCode()
		""".trimIndent()

        try {
            val result = DreamScriptManager.evaluate<Any>(plugin, content)
            result::class.java.getMethod("doStuff", Player::class.java).invoke(result, player)
        } catch (e: Exception) {
            e.printStackTrace()
            player.sendMessage("§dDeu ruim! ${e.message}")
        }
    }
}