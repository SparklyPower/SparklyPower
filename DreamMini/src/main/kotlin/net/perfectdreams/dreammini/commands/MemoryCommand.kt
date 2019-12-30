package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import java.lang.management.ManagementFactory
import java.util.*

class MemoryCommand(val m: DreamMini) : SparklyCommand(arrayOf("memory", "mem", "uptime"), permission = "dreammini.memory"){

	@Subcommand
	fun root(sender: CommandSender, args: Array<String>){
		val arg = args.getOrNull(0)

		if(arg == "worlds"){
			for (world in Bukkit.getWorlds()) {
				val type = when (world.environment) {
					World.Environment.NORMAL -> "Overworld"
					World.Environment.NETHER -> "Nether"
					World.Environment.THE_END -> "The End"
					else -> "???"
				}

				var tileEntities = 0
				world.loadedChunks.forEach {
					tileEntities += it.tileEntities.size
				}

				sender.sendMessage("§e${world.name} §b$type")
				sender.sendMessage("§8• §bChunks: §3${world.loadedChunks.size} §bEntities: §3${world.entities.size} §bTile Entities: §3${tileEntities}")
			}
		}

		sender.sendMessage("§bUptime: §3${formatDateDiff(ManagementFactory.getRuntimeMXBean().startTime)}")
		sender.sendMessage("§bMemória Máxima: §3${(Runtime.getRuntime().maxMemory() / 1024 / 1024)}MB")
		sender.sendMessage("§bMemória Alocada: §3${(Runtime.getRuntime().totalMemory() / 1024 / 1024)}MB")
		sender.sendMessage("§bMemória Livre: §3${(Runtime.getRuntime().freeMemory() / 1024 / 1024)}MB")
		sender.sendMessage("§7Para ver mais informações, use §6/memory worlds")
	}

	private val maxYears = 100000

	private fun dateDiff(type: Int, fromDate: Calendar, toDate: Calendar, future: Boolean): Int {
		val year = Calendar.YEAR

		val fromYear = fromDate.get(year)
		val toYear = toDate.get(year)
		if (Math.abs(fromYear - toYear) > maxYears) {
			toDate.set(year, fromYear + if (future) maxYears else -maxYears)
		}

		var diff = 0
		var savedDate = fromDate.timeInMillis
		while (future && !fromDate.after(toDate) || !future && !fromDate.before(toDate)) {
			savedDate = fromDate.timeInMillis
			fromDate.add(type, if (future) 1 else -1)
			diff++
		}
		diff--
		fromDate.timeInMillis = savedDate
		return diff
	}

	private fun formatDateDiff(date: Long): String {
		val c = GregorianCalendar()
		c.timeInMillis = date
		val now = GregorianCalendar()
		return formatDateDiff(now, c)
	}

	private fun formatDateDiff(fromDate: Calendar, toDate: Calendar): String {
		var future = false
		if (toDate == fromDate) {
			return "agora"
		}
		if (toDate.after(fromDate)) {
			future = true
		}
		val sb = StringBuilder()
		val types = intArrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND)
		val names = arrayOf<String>("ano", "anos", "mês", "meses", "dia", "dias", "hora", "horas", "minuto", "minutos", "segundo", "segundos")
		var accuracy = 0
		for (i in types.indices) {
			if (accuracy > 2) {
				break
			}
			val diff = dateDiff(types[i], fromDate, toDate, future)
			if (diff > 0) {
				accuracy++
				sb.append(" ").append(diff).append(" ").append(names[i * 2 + (if (diff > 1) 1 else 0)])
			}
		}
		return if (sb.length == 0) {
			"agora"
		} else sb.toString().trim { it <= ' ' }
	}
}