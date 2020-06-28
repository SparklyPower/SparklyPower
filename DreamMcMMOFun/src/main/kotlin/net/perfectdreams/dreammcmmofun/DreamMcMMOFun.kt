package net.perfectdreams.dreammcmmofun

import com.gmail.nossr50.datatypes.skills.PrimarySkillType
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

class DreamMcMMOFun : KotlinPlugin(), Listener {
	val lastOnlineTimeCheck = mutableMapOf<Player, Int>()

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		lastOnlineTimeCheck.remove(e.player)
	}

	override fun softEnable() {
		super.softEnable()

		registerEvents(this)

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

	private fun getTopPlayerInMcMMOSkill(skill: PrimarySkillType?) = mcMMO.getDatabaseManager().readLeaderboard(skill, 1, 1).firstOrNull()?.name
}