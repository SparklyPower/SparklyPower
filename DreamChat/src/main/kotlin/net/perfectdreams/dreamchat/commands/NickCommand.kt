package net.perfectdreams.dreamchat.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.tables.ChatUsers
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.exposed.upsert
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.stripColorCode
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class NickCommand(val m: DreamChat) : AbstractCommand("nick", listOf("nickname"), "dreamchat.nick") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage(generateCommandInfo("nick", mapOf("nickname" to "Seu novo nome")))
	}

	@Subcommand
	fun nick(sender: Player, newUsername: String) {
		if (newUsername == "off" || newUsername == "tirar" || newUsername == "remover") {
			sender.sendMessage("§aSeu nickname personalizado foi retirado!")

			sender.displayName = null
			sender.playerListName = null

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
		val realLength = colorizedUsername.stripColorCode().length

		if (realLength > 32)
			throw ExecutedCommandException("§cSeu novo nickname é grande demais!")

		sender.displayName = colorizedUsername
		sender.playerListName = colorizedUsername

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