package net.perfectdreams.dreamvote.commands

import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamvote.DreamVote
import org.bukkit.entity.Player
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import java.io.File
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamvote.dao.Vote
import net.perfectdreams.dreamvote.tables.Votes
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class VotarCommand(val m: DreamVote) : SparklyCommand(arrayOf("votar", "vote")) {
	@Subcommand()
	fun root(player: Player) {
		player.sendMessage("§aVote em nosso servidor!§3 https://sparklypower.net/votar")
	}

	@Subcommand(["plzvote"])
	@SubcommandPermission("dreamvote.setup")
	fun plzvote(player: Player) {
		player.sendMessage("Forçando pedido de voto...")

		m.broadcastPleaseVoteMessage()
	}

	@Subcommand(["forcevote"])
	@SubcommandPermission("dreamvote.setup")
	fun forceVote(player: Player) {
		player.sendMessage("Forçando voto...")

		m.giveVoteAward(player.uniqueId, "Forced Vote")
	}

	@Subcommand(["reload"])
	@SubcommandPermission("dreamvote.setup")
	fun reload(player: Player) {
		m.alwaysAwards = DreamUtils.gson.fromJson(File(m.dataFolder, "alwaysawards.json").readText())
		m.randomAwards = DreamUtils.gson.fromJson(File(m.dataFolder, "randomawards.json").readText())

		player.sendMessage("§aRecarregado com sucesso!")
	}

	@Subcommand(["give_index"])
	@SubcommandPermission("dreamvote.setup")
	fun give(player: Player, idx: Int) {
		val reward = DreamVote.INSTANCE.randomAwards[idx]

		for (item in reward.items) {
			player.inventory.addItem(item)
		}

		player.sendMessage("§aRewards dado!")
	}

	@Subcommand(["give"])
	@SubcommandPermission("dreamvote.setup")
	fun giveQuery(player: Player, args: Array<String>) {
		val query = args.joinToString(" ")
		val rewards = DreamVote.INSTANCE.randomAwards.filter { it.name.contains(query, true) }

		val drop = mutableListOf<ItemStack>()
		for (reward in rewards) {
			for (item in reward.items) {
				if (player.inventory.canHoldItem(item)) {
					player.inventory.addItem(item)
				} else {
					drop.add(item)
				}
			}
		}

		for (item in drop) {
			player.world.dropItem(player.location, item)
		}

		player.sendMessage("§aRewards dados!")
	}
	
	@Subcommand(["lookup_votes"])
	@SubcommandPermission("dreamvote.setup")
	fun lookup(sender: CommandSender, target: String) {
		val targetuuid = try { UUID.fromString(target) } catch (e: IllegalStateException) { UUID.nameUUIDFromBytes("OfflinePlayer:$target".toByteArray(Charsets.UTF_8)) } 
		
		val voteCount = m.getVoteCount(targetuuid)
		
		sender.sendMessage("§cVotos de §e$targetuuid§c: §e$voteCount votos")
	}

	@Subcommand(["vote_rematch"])
	@SubcommandPermission("dreamvote.setup")
	fun voteRematch(sender: CommandSender, target: String) {
		scheduler().schedule(m, SynchronizationContext.ASYNC) {
			sender.sendMessage("Dando pesadelos para as pessoas que votaram...")

			val userIdCount = Votes.player.count()

			val topVoterUsers = transaction(Databases.databaseNetwork) {
				Votes.slice(Votes.player, userIdCount).selectAll()
					.groupBy(Votes.player)
					.orderBy(
						userIdCount to SortOrder.DESC
					)
					.toMutableList()
			}

			var idx = 0
			sender.sendMessage("Existem ${topVoterUsers.size} pessoas que receberão pesadelos!")

			topVoterUsers.forEach {
				if (idx % 250 == 0) {
					sender.sendMessage("$idx/${topVoterUsers.size} já receberam os pesadelos...")
				}

				Cash.giveCash(it[Votes.player], (it[userIdCount] * 15), TransactionContext(type = TransactionType.VOTE_REWARDS))

				idx++
			}
		}
	}
}
