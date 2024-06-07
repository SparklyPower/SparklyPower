package net.perfectdreams.dreamchat.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.tables.ChatUsers
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert

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
					// Upsert was causing issues with the entire field being replaced
					ChatUsers.update({ ChatUsers.id eq sender.uniqueId }) {
						it[ChatUsers._id] = sender.uniqueId
						it[ChatUsers.tag] = null
					}
				}
			}
			return
		}

		val colorizedTag = newUsername.translateColorCodes()
		val realLength = colorizedTag.stripColors()!!.length

		if (realLength > 16)
			throw ExecutedCommandException("§cSua nova tag é grande demais!")

		sender.sendMessage("§aSua tag foi alterada para \"${colorizedTag}§r§a\"!")

		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			transaction {
				// Upsert was causing issues with the entire field being replaced
				val updatedRows = ChatUsers.update({ ChatUsers.id eq sender.uniqueId }) {
					it[ChatUsers._id] = sender.uniqueId
					it[ChatUsers.tag] = colorizedTag
				}

				if (updatedRows == 0) {
					ChatUsers.insert {
						it[ChatUsers.id] = sender.uniqueId
						it[ChatUsers.tag] = colorizedTag
					}
				}
			}
		}
	}
}