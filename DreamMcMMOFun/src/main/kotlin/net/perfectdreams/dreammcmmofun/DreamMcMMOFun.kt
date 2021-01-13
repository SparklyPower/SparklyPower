package net.perfectdreams.dreammcmmofun

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.events.chat.McMMOPartyChatEvent
import com.gmail.nossr50.mcMMO
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

class DreamMcMMOFun : KotlinPlugin(), Listener {
	val lastOnlineTimeCheck = mutableMapOf<Player, Int>()

	private val boostTimes = listOf(
		McMMOXPBoostTime(8, 10),
		McMMOXPBoostTime(14, 16),
		McMMOXPBoostTime(20, 22)
	)

	override fun softEnable() {
		super.softEnable()

		registerEvents(this)

		schedule {
			while (true) {
				switchContext(SynchronizationContext.ASYNC)

				val today = Instant.now()
					.atZone(ZoneId.of("America/Sao_Paulo"))

				val currentDay = today.dayOfWeek
				val currentHour = today.hour

				val boostIndex = when (currentDay) {
					DayOfWeek.MONDAY -> 0
					DayOfWeek.TUESDAY -> -1
					DayOfWeek.WEDNESDAY -> 1
					DayOfWeek.THURSDAY -> -1
					DayOfWeek.FRIDAY -> 2
					DayOfWeek.SATURDAY -> -1
					DayOfWeek.SUNDAY -> -1
				}

				if (boostIndex != -1) {
					val boostTime = boostTimes[boostIndex]

					if (currentHour in boostTime.startsAt until boostTime.endsAt && !mcMMO.p.isXPEventEnabled) {
						switchContext(SynchronizationContext.SYNC)
						// We could use McMMO's API but it doesn't broadcast
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "xprate 2 true")
						switchContext(SynchronizationContext.ASYNC)

						sendMcMMOBoostInformationToDiscord(boostTime.endsAt)
					}

					if (currentHour == boostTime.endsAt && mcMMO.p.isXPEventEnabled) {
						switchContext(SynchronizationContext.SYNC)
						// We could use McMMO's API but it doesn't broadcast
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "xprate reset")
						switchContext(SynchronizationContext.ASYNC)
					}
				}

				waitFor(100L)
			}
		}

		schedule {
			while (true) {
				switchContext(SynchronizationContext.SYNC)

				val totalTwoHoursPlayers = Bukkit.getOnlinePlayers()
					.filter {
						val lastTimeCheck = lastOnlineTimeCheck[it]

						if (lastTimeCheck != null) {
							val modBefore = (((lastOnlineTimeCheck[it]!!) / 20) / 60) % 120
							val modNow = (((it.getStatistic(Statistic.PLAY_ONE_MINUTE)) / 20) / 60) % 120

							if (modBefore > modNow) {
								logger.info("${it.name} matches! Mod before is $modBefore, Mod now is $modNow")
								return@filter true
							}
						}
						return@filter false
					}

				logger.info("Total two hours players right now: ${totalTwoHoursPlayers.joinToString { it.name }}")

				val topPlayerSkills = mutableMapOf<PrimarySkillType, String?>()

				switchContext(SynchronizationContext.ASYNC)
				for (skill in PrimarySkillType.values()
					.filter { it != PrimarySkillType.SALVAGE && it != PrimarySkillType.SMELTING }) {
					topPlayerSkills[skill] = getTopPlayerInMcMMOSkill(skill)
				}

				topPlayerSkills.forEach { (skill, playerName) ->
					logger.info("Skill $skill top player is $playerName")
				}

				switchContext(SynchronizationContext.SYNC)

				totalTwoHoursPlayers
					.forEach {
						val matchingSkill = topPlayerSkills.filter { (_, playerName) -> it.name.equals(playerName, true) }

						for ((skill, playerName) in matchingSkill) {
							switchContext(SynchronizationContext.ASYNC)
							Cash.giveCash(it, 1)
							logger.info("Giving 1 pesadelo to ${it.name} because they are top ${skill}!")
							it.sendMessage("§aVocê ganhou um pesadelo por ser top skill em ${skill}!")
							switchContext(SynchronizationContext.SYNC)
						}
					}

				Bukkit.getOnlinePlayers().forEach {
					lastOnlineTimeCheck[it] = it.getStatistic(Statistic.PLAY_ONE_MINUTE)
				}

				waitFor(20 * 60)
			}
		}
	}

	private fun sendMcMMOBoostInformationToDiscord(endsAt: Int) {
		WebhookClient.withUrl(config.getString("xp-boost-webhook-url")!!)
			.use {
				it.send(
					WebhookMessageBuilder()
						.setUsername("Pantufa")
						.setAvatarUrl("https://cdn.discordapp.com/avatars/390927821997998081/c6917c2ad778119eb041002c6e18f581.png?size=2048")
						.setContent("<@&798696876052185119> <:lori_yay_ping:640141673531441153>\n\nCatapimbas! O que é isso?!?!?!?!\n\nExatamente, <a:dokyo_bongo:539839128674631690> **Boost de 2x na experiência do McMMO!** <a:dokyo_bongo:539839128674631690>\n\nAté as $endsAt:00, então aproveitem. <:lori_ok:731873534036541500>")
						.build()
				)
			}
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		lastOnlineTimeCheck.remove(e.player)
	}

	@EventHandler
	fun onChat(e: McMMOPartyChatEvent) {
		for (staff in Bukkit.getOnlinePlayers().asSequence().filter { it.hasPermission("dreammcmmofun.snoop") }) {
			staff.sendMessage("§7[${e.author.examinableName()} » Party ${e.authorParty.name}] ${e.componentMessage.content()}")
		}
	}

	private fun getTopPlayerInMcMMOSkill(skill: PrimarySkillType?) = mcMMO.getDatabaseManager().readLeaderboard(skill, 1, 1).firstOrNull()?.name
	private data class McMMOXPBoostTime(val startsAt: Int, val endsAt: Int)
}