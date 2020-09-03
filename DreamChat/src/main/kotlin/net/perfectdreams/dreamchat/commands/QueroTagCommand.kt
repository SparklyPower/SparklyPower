package net.perfectdreams.dreamchat.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.tables.ChatUsers
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.exposed.upsert
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class QueroTagCommand(val m: DreamChat) : SparklyCommand(arrayOf("querotag"), "dreamchat.querotag") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage(generateCommandInfo("nick", mapOf("tag" to "Sua nova tag")))
	}

	@Subcommand
	fun nick(sender: Player, newUsername: String) {
		if (newUsername == "off" || newUsername == "tirar" || newUsername == "remover") {
			sender.sendMessage("§aSua tag personalizada foi retirada!")

			scheduler().schedule(m, SynchronizationContext.ASYNC) {
				transaction {
					ChatUsers.upsert(ChatUsers.id) {
						it[ChatUsers._id] = sender.uniqueId
						it[tag] = null
					}
				}
			}
			return
		}

		val colorizedUsername = newUsername.translateColorCodes()
		val realLength = colorizedUsername.stripColors()!!.length

		if (realLength > 16)
			throw ExecutedCommandException("§cSua nova tag é grande demais!")

		sender.sendMessage("§aSua tag foi alterada para \"${colorizedUsername}§r§a\"!")

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction {
				ChatUsers.upsert(ChatUsers.id) {
					it[ChatUsers._id] = sender.uniqueId
					it[tag] = colorizedUsername
				}
			}
		}
	}
}