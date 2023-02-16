package net.perfectdreams.dreamcore.utils

import com.google.common.base.Splitter
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

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
		scoreboard.getTeam("line1")!!.addEntry(getColorCodeForLine(1))
		scoreboard.registerNewTeam("line2")
		scoreboard.getTeam("line2")!!.addEntry(getColorCodeForLine(2))
		scoreboard.registerNewTeam("line3")
		scoreboard.getTeam("line3")!!.addEntry(getColorCodeForLine(3))
		scoreboard.registerNewTeam("line4")
		scoreboard.getTeam("line4")!!.addEntry(getColorCodeForLine(4))
		scoreboard.registerNewTeam("line5")
		scoreboard.getTeam("line5")!!.addEntry(getColorCodeForLine(5))
		scoreboard.registerNewTeam("line6")
		scoreboard.getTeam("line6")!!.addEntry(getColorCodeForLine(6))
		scoreboard.registerNewTeam("line7")
		scoreboard.getTeam("line7")!!.addEntry(getColorCodeForLine(7))
		scoreboard.registerNewTeam("line8")
		scoreboard.getTeam("line8")!!.addEntry(getColorCodeForLine(8))
		scoreboard.registerNewTeam("line9")
		scoreboard.getTeam("line9")!!.addEntry(getColorCodeForLine(9))
		scoreboard.registerNewTeam("line10")
		scoreboard.getTeam("line10")!!.addEntry(getColorCodeForLine(10))
		scoreboard.registerNewTeam("line11")
		scoreboard.getTeam("line11")!!.addEntry(getColorCodeForLine(11))
		scoreboard.registerNewTeam("line12")
		scoreboard.getTeam("line12")!!.addEntry(getColorCodeForLine(12))
		scoreboard.registerNewTeam("line13")
		scoreboard.getTeam("line13")!!.addEntry(getColorCodeForLine(13))
		scoreboard.registerNewTeam("line14")
		scoreboard.getTeam("line14")!!.addEntry(getColorCodeForLine(14))
		scoreboard.registerNewTeam("line15")
		scoreboard.getTeam("line15")!!.addEntry(getColorCodeForLine(15))
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
			val currentScore = scoreboard.getObjective("alphys")!!.getScore(getColorCodeForLine(line))
			currentScore.score = line

			lineVisibility[line] = true
		}
	}

	fun removeLine(line: Int) {
		scoreboard.resetScores(getColorCodeForLine(line))
		lineVisibility[line] = false
	}

	fun setTitle(title: String) {
		scoreboard.getObjective("alphys")!!.displayName = title
	}

	fun getColorCodeForLine(line: Int): String {
		when (line) {
			15 -> return "§0"
			14 -> return "§1"
			13 -> return "§2"
			12 -> return "§3"
			11 -> return "§4"
			10 -> return "§5"
			9 -> return "§6"
			8 -> return "§7"
			7 -> return "§8"
			6 -> return "§9"
			5 -> return "§a"
			4 -> return "§b"
			3 -> return "§c"
			2 -> return "§d"
			1 -> return "§e"
			else -> error("Unsupported line ID $line")
		}
	}
}

