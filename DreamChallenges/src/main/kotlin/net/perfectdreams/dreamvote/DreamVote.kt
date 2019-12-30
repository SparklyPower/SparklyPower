package net.perfectdreams.dreamvote

import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.DreamUtils.gson
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamvote.commands.VotarCommand
import net.perfectdreams.dreamvote.dao.Vote
import net.perfectdreams.dreamvote.listeners.TagListener
import net.perfectdreams.dreamvote.listeners.VoteListener
import net.perfectdreams.dreamvote.tables.Votes
import net.perfectdreams.dreamvote.utils.VoteAward
import net.perfectdreams.libs.com.mongodb.client.MongoCollection
import net.perfectdreams.libs.com.mongodb.client.model.Filters
import net.perfectdreams.libs.org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPlayerExact
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

class DreamVote : KotlinPlugin() {
	companion object {
		val INSTANCE get() = Bukkit.getPluginManager().getPlugin("DreamVote") as DreamVote
	}

	var alwaysAwards = mutableListOf<VoteAward>()
	var randomAwards = mutableListOf<VoteAward>()
	var lastVoter: String? = null
	var earnedMoney = 0.0

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
					Votes
			)
		}

		dataFolder.mkdirs()

		alwaysAwards = gson.fromJson(File(dataFolder, "alwaysawards.json").readText())
		randomAwards = gson.fromJson(File(dataFolder, "randomawards.json").readText())

		registerCommand(VotarCommand(this))
		registerEvents(TagListener(this))
		registerEvents(VoteListener(this))

		scheduler().schedule(this) {
			while (true) {
				val lastVoter = lastVoter
				if (lastVoter != null) {
					VaultUtils.econ.depositPlayer(lastVoter, 15.0)
					earnedMoney += 15.0
					if (earnedMoney % 180.0 == 165.0) {
						Bukkit.broadcastMessage("§b$lastVoter§d já ganhou §2$earnedMoney sonhos§d apenas mantendo a tag de §c§lÚltimo Votador§d, roube a tag votando! §6/votar")
					}
				}
				waitFor(5 * 20)
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun getVoteCount(player: Player): Int {
		return getVoteCount(player.uniqueId)
	}

	fun getVoteCount(uuid: UUID): Int {
		DreamUtils.assertAsyncThread(true)

		return transaction(Databases.databaseNetwork) {
			Votes.select {
				Votes.player eq uuid
			}.count()
		}
	}

	fun giveVoteAward(username: String, serviceName: String, broadcast: Boolean = true) {
		giveVoteAward(Bukkit.getOfflinePlayer(username).uniqueId, serviceName, broadcast)
	}

	fun giveVoteAward(uniqueId: UUID, serviceName: String, broadcast: Boolean = true) {
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			val voteCount = getVoteCount(uniqueId)
			val giveAwards = mutableListOf<VoteAward>()

			transaction(Databases.databaseNetwork) {
				Vote.new {
					this.player = uniqueId
					this.votedAt = System.currentTimeMillis()
					this.website = serviceName
				}
			}

			giveAwards.addAll(alwaysAwards.filter {
				if (it.hasEqualsVoteCountCondition) {
					return@filter (it.requiredEqualsVoteCount == voteCount)
				}
				true
			})

			// Adicionar award aleatório
			giveAwards.add(randomAwards.getRandom())

			val items = mutableListOf<ItemStack>()
			var money = 0.0

			giveAwards.forEach {
				money += it.money.toDouble()
				items.addAll(it.items)
			}

			switchContext(SynchronizationContext.SYNC)
			earnedMoney = 0.0

			// Dar itens para o player
			// gambiarra temporária
			items.forEach {
				Bukkit.getPlayer(uniqueId).inventory.addItem(it)
			}

			// DreamCorreios.addItems(username, true, true, *items.toTypedArray())
			val offlinePlayer = Bukkit.getOfflinePlayer(uniqueId)

			// Depositar a grana
			VaultUtils.econ.depositPlayer(offlinePlayer, money)

			if (broadcast) {
				lastVoter = offlinePlayer.name
				val prizes = "§9" + giveAwards.filter { !it.hidden }.joinToString("§e, §9", transform = { it.name })

				// Como o player pode estar (ou não!) online, nós iremos pegar o displayName apenas caso o player esteja online
				val player = getPlayerExact(lastVoter)
				val playerName = player?.displayName ?: lastVoter

				Bukkit.broadcastMessage("§6➠ §b$playerName §evotou no §4§lSparkly§b§lPower§e e ganhou $prizes§e! Vote você também! §6/votar")
				player?.sendTitle("§aParabéns!", "§eVocê ganhou $prizes", 10, 60, 10)
			}

			switchContext(SynchronizationContext.ASYNC)

			Cash.giveCash(uniqueId, 7)

			Webhooks.PANTUFA_INFO.send(DiscordMessage(
					content = "**$lastVoter** votou, agora **$lastVoter** possui ${voteCount + 1} votos. *Prêmios recebidos:* ${giveAwards.joinToString(", ", transform = { "`${it.name}`" })}"
			))
		}
	}
}