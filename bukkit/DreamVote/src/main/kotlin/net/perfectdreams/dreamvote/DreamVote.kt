package net.perfectdreams.dreamvote

import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.DreamEventManager
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.DreamUtils.gson
import net.perfectdreams.dreamcore.utils.discord.DiscordMessage
import net.perfectdreams.dreamvote.commands.VotarCommand
import net.perfectdreams.dreamvote.dao.Vote
import net.perfectdreams.dreamvote.listeners.TagListener
import net.perfectdreams.dreamvote.listeners.VoteListener
import net.perfectdreams.dreamvote.tables.Votes
import net.perfectdreams.dreamvote.utils.VoteAward
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPlayerExact
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
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

		lastVoter = this.config.getString("last-voter")
		earnedMoney = this.config.getDouble("earned-money", 0.0)

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
						Bukkit.broadcastMessage("§b$lastVoter§d já ganhou §2$earnedMoney sonecas§d apenas mantendo a tag de §c§lÚltimo Votador§d, roube a tag votando! §6/votar")
					}
				}
				waitFor(5 * 20)
			}
		}

		scheduler().schedule(this) {
			while (true) {
				// One hour
				waitFor(3600 * 20)

				// Only broadcast if there aren't any events happening, this avoids a huge title showing up in the user's screen while they are
				// participating in an event
				while (DreamCore.INSTANCE.dreamEventManager.getRunningEvents().isNotEmpty())
					waitFor(20L)

				broadcastPleaseVoteMessage()
			}
		}
	}

	override fun softDisable() {
		super.softDisable()

		this.config.set("last-voter", lastVoter)
		this.config.set("earned-money", earnedMoney)
		this.saveConfig()
	}

	fun getVoteCount(player: Player) = getVoteCount(player.uniqueId)

	fun getVoteCount(uuid: UUID): Long {
		DreamUtils.assertAsyncThread(true)

		return transaction(Databases.databaseNetwork) {
			Votes.select {
				Votes.player eq uuid
			}.count()
		}
	}

	fun hasVotedToday(player: Player) = hasVotedToday(player.uniqueId)

	fun hasVotedToday(uuid: UUID): Boolean {
		DreamUtils.assertAsyncThread(true)

		val today = Calendar.getInstance()
		today.set(Calendar.HOUR_OF_DAY, 0)
		today.set(Calendar.MINUTE, 0)
		today.set(Calendar.SECOND, 0)
		today.set(Calendar.MILLISECOND, 0)

		return transaction(Databases.databaseNetwork) {
			Votes.select {
				Votes.votedAt greaterEq today.timeInMillis and (Votes.player eq uuid)
			}.count() != 0L
		}
	}

	fun giveVoteAward(username: String, serviceName: String, broadcast: Boolean = true) {
		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			val uniqueId = DreamUtils.retrieveUserUniqueId(username.trim())
			giveVoteAward(uniqueId, serviceName, broadcast)
		}
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
					return@filter (it.requiredEqualsVoteCount.toLong() == voteCount)
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

			val player = Bukkit.getPlayer(uniqueId)

			if (player != null) {
				// Dar itens para o player
				// gambiarra temporária
				items.forEach {
					player.inventory.addItem(it)
				}
			}

			// DreamCorreios.addItems(username, true, true, *items.toTypedArray())
			val offlinePlayer = Bukkit.getOfflinePlayer(uniqueId)

			// Depositar a grana
			VaultUtils.econ.depositPlayer(offlinePlayer, money)

			if (broadcast) {
				val lastVoter = offlinePlayer.name
				this@DreamVote.lastVoter = lastVoter

				if (lastVoter != null) {
					val prizes = "§9" + giveAwards.filter { !it.hidden }.joinToString("§e, §9", transform = { it.name })

					// Como o player pode estar (ou não!) online, nós iremos pegar o displayName apenas caso o player esteja online
					val player = getPlayerExact(lastVoter)
					val playerName = player?.displayName ?: lastVoter

					Bukkit.broadcastMessage("§6➠ §b$playerName §evotou no §4§lSparkly§b§lPower§e e ganhou $prizes§e! Vote você também! §6/votar")
					player?.sendTitle("§aParabéns!", "§eVocê ganhou $prizes", 10, 60, 10)
				}
			}

			switchContext(SynchronizationContext.ASYNC)

			Cash.giveCash(uniqueId, 7)

			Webhooks.PANTUFA_INFO?.send(DiscordMessage(
					content = "**$lastVoter** votou, agora **$lastVoter** possui ${voteCount + 1} votos. *Prêmios recebidos:* ${giveAwards.joinToString(", ", transform = { "`${it.name}`" })}"
			))
		}
	}

	fun broadcastPleaseVoteMessage() {
		schedule {
			var sentToPlayers = 0
			for (player in Bukkit.getOnlinePlayers()) {
				switchContext(SynchronizationContext.ASYNC)

				val hasVotedToday = INSTANCE.hasVotedToday(player)

				if (!hasVotedToday) {
					sentToPlayers++
					switchContext(SynchronizationContext.SYNC)
					schedule {
						player.sendTitle("§eEntão...", "§aVocê tá afim de uns §3diamantes§a? §f锃", 20, 200, 20)
						waitFor(20 + 100 + 20)
						player.sendTitle(
							"§eSim, §3diamantes§e de graça!",
							"§aE ainda poder conseguir §b§lVIP§a sem pagar §cnada§a? §f锇",
							20,
							200,
							20
						)
						waitFor(20 + 100 + 20)
						player.sendTitle(
							"§eEntão §dvote no servidor§e!",
							"§aSimples e fácil, §6/votar §f锈",
							20,
							200,
							20
						)
						waitFor(20 + 100 + 20)
						player.sendTitle(
							"§eE lembre-se...",
							"§aVotar ajuda o servidor e você pode votar todos os dias! §f镾",
							20,
							200,
							20
						)
					}
				}
			}

			Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }.forEach {
				it.sendMessage("§aEnviando mendigação de votos para $sentToPlayers players!")
			}
		}
	}
}
