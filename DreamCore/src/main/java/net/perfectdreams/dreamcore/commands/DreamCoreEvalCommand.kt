package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.stripColorCode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import java.io.File

object DreamCoreEvalCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("dreamcore eval")) {
        permission = "dreamcore.setup"

        executes {
            val heldItem = player.inventory.itemInMainHand

            if (heldItem.type != Material.WRITABLE_BOOK && heldItem.type != Material.WRITTEN_BOOK) {
                throw ExecutedCommandException("§cVocê precisa estar segurando um livro!")
            }

            val bookMeta = heldItem.itemMeta as BookMeta
            val lines = bookMeta.pages.map { it.stripColorCode() }.joinToString("\n")

            sender.sendMessage("§dExecutando...")
            sender.sendMessage(lines)

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
                result::class.java.getMethod("doStuff", Player::class.java).invoke(result, sender)
            } catch (e: Exception) {
                e.printStackTrace()
                sender.sendMessage("§dDeu ruim! ${e.message}")
            }
        }
    }
}