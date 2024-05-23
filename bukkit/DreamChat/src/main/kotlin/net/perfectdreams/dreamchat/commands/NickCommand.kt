package net.perfectdreams.dreamchat.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.tables.ChatUsers
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

class NickCommand(val m: DreamChat) : AbstractCommand("nick", listOf("nickname"), "dreamchat.nick") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage(generateCommandInfo("nick", mapOf("nickname" to "Seu novo nome")))
	}

	@Subcommand
	fun nick(sender: Player, newUsername: String) {
		if (newUsername == "off" || newUsername == "tirar" || newUsername == "remover") {
			sender.sendMessage("§aSeu nickname personalizado foi retirado!")

			sender.setDisplayName(null)
			sender.setPlayerListName(null)

			scheduler().schedule(m, SynchronizationContext.ASYNC) {
				transaction {
					ChatUsers.upsert(ChatUsers.id) {
						it[nickname] = null
					}
				}
			}
			return
		}

		val colorizedUsername = newUsername.translateColorCodes()
		val realLength = colorizedUsername.stripColors()!!.length

		if (realLength > 32)
			throw ExecutedCommandException("§cSeu novo nickname é grande demais!")

		sender.setDisplayName(colorizedUsername)
		sender.setPlayerListName(colorizedUsername)

		sender.sendMessage("§aSeu nickname foi alterado para \"${colorizedUsername}§r§a\"!")

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction {
				ChatUsers.upsert(ChatUsers.id) {
					it[ChatUsers._id] = sender.uniqueId
					it[nickname] = sender.displayName
				}
			}
		}
	}
}