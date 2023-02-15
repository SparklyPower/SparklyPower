package net.perfectdreams.dreamcore.utils

import com.google.common.base.Splitter
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.lang.IllegalArgumentException
import java.util.*

/**
 * PhoenixScoreboard - Uma scoreboard sem flickers, usando teams
 *
 * @author MrPowerGamerBR
 */
class PhoenixScoreboard {
	var scoreboard: Scoreboard

	/**
	 * Used to avoid getting and setting the score in the scoreboard multiple times for no reason
	 */
	private val lineVisibility = mutableMapOf<Int, Boolean>()

	init {
		scoreboard = Bukkit.getScoreboardManager().newScoreboard
		scoreboard.registerNewObjective("alphys", "dummy")
		scoreboard.getObjective("alphys")!!.displaySlot = DisplaySlot.SIDEBAR
		scoreboard.registerNewTeam("line1")
		scoreboard.getTeam("line1")!!.addPlayer(line1)
		scoreboard.registerNewTeam("line2")
		scoreboard.getTeam("line2")!!.addPlayer(line2)
		scoreboard.registerNewTeam("line3")
		scoreboard.getTeam("line3")!!.addPlayer(line3)
		scoreboard.registerNewTeam("line4")
		scoreboard.getTeam("line4")!!.addPlayer(line4)
		scoreboard.registerNewTeam("line5")
		scoreboard.getTeam("line5")!!.addPlayer(line5)
		scoreboard.registerNewTeam("line6")
		scoreboard.getTeam("line6")!!.addPlayer(line6)
		scoreboard.registerNewTeam("line7")
		scoreboard.getTeam("line7")!!.addPlayer(line7)
		scoreboard.registerNewTeam("line8")
		scoreboard.getTeam("line8")!!.addPlayer(line8)
		scoreboard.registerNewTeam("line9")
		scoreboard.getTeam("line9")!!.addPlayer(line9)
		scoreboard.registerNewTeam("line10")
		scoreboard.getTeam("line10")!!.addPlayer(line10)
		scoreboard.registerNewTeam("line11")
		scoreboard.getTeam("line11")!!.addPlayer(line11)
		scoreboard.registerNewTeam("line12")
		scoreboard.getTeam("line12")!!.addPlayer(line12)
		scoreboard.registerNewTeam("line13")
		scoreboard.getTeam("line13")!!.addPlayer(line13)
		scoreboard.registerNewTeam("line14")
		scoreboard.getTeam("line14")!!.addPlayer(line14)
		scoreboard.registerNewTeam("line15")
		scoreboard.getTeam("line15")!!.addPlayer(line15)
	}

	fun setText(text: String, line: Int) {
		if (line !in 1..15)
			throw IllegalArgumentException("Line $line with text \"$text\" is outside of range 1..15!")

		val divided = Splitter.fixedLength(16).split(text).iterator()

		// This code tries to optimize "Scoreboard score search" by avoiding unnecessary updates
		// How we do this? By checking if the team prefix/suffix is exactly the same as before
		// This avoids a lot of unnecessary packet updates to the client!
		val firstHalf = divided.next()

		val firstHalfTeam = scoreboard.getTeam("line$line")!!

		if (firstHalfTeam.prefix != firstHalf)
			firstHalfTeam.prefix = firstHalf

		val secondHalfTeam = scoreboard.getTeam("line$line")!!

		if (divided.hasNext()) {
			val color = ChatColor.getLastColors(firstHalfTeam.prefix)

			val newSuffix = color + divided.next()

			if (secondHalfTeam.suffix != newSuffix)
				secondHalfTeam.suffix = newSuffix
		} else if (secondHalfTeam.suffix != "") {
			secondHalfTeam.suffix = ""
		}

		// We don't need to get and set the score EVERY SINGLE TIME, most of the time it is a constant value that never changes
		// So, to avoid unnecessary calls, we are going to cache the values and only changing them if needed
		val isVisible = lineVisibility[line] ?: false

		if (!isVisible) {
			val currentScore = scoreboard.getObjective("alphys")!!.getScore(getOfflinePlayerForLine(line)!!)
			currentScore.score = line

			lineVisibility[line] = true
		}
	}

	fun removeLine(line: Int) {
		scoreboard.resetScores(getOfflinePlayerForLine(line)!!)
		lineVisibility[line] = false
	}

	fun setTitle(title: String) {
		scoreboard.getObjective("alphys")!!.displayName = title
	}

	companion object {
		lateinit var line1: OfflinePlayer
		lateinit var line2: OfflinePlayer
		lateinit var line3: OfflinePlayer
		lateinit var line4: OfflinePlayer
		lateinit var line5: OfflinePlayer
		lateinit var line6: OfflinePlayer
		lateinit var line7: OfflinePlayer
		lateinit var line8: OfflinePlayer
		lateinit var line9: OfflinePlayer
		lateinit var line10: OfflinePlayer
		lateinit var line11: OfflinePlayer
		lateinit var line12: OfflinePlayer
		lateinit var line13: OfflinePlayer
		lateinit var line14: OfflinePlayer
		lateinit var line15: OfflinePlayer

		fun init() {
			PhoenixScoreboard.line1 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§a§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line2 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§6§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line3 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§7§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line4 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§d§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line5 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§4§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line6 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§8§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line7 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§3§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line8 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§c§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line9 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§f§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line10 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§e§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line11 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§6§e§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line12 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§6§f§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line13 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§6§1§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line14 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§6§2§f".toByteArray(Charsets.UTF_8)))
			PhoenixScoreboard.line15 = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes("OfflinePlayer:§6§3§f".toByteArray(Charsets.UTF_8)))
		}

		fun getOfflinePlayerForLine(line: Int): OfflinePlayer? {
			when (line) {
				15 -> return line15
				14 -> return line14
				13 -> return line13
				12 -> return line12
				11 -> return line11
				10 -> return line10
				9 -> return line9
				8 -> return line8
				7 -> return line7
				6 -> return line6
				5 -> return line5
				4 -> return line4
				3 -> return line3
				2 -> return line2
				1 -> return line1
				else -> return null
			}
		}
	}
}
