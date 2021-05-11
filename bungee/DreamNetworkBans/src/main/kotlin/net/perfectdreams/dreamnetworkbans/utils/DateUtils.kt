package net.perfectdreams.dreamnetworkbans.utils

import java.util.*

object DateUtils {
	
	private val maxYears = 100000
	
	fun dateDiff(type: Int, fromDate: Calendar, toDate: Calendar, future: Boolean): Int {
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
	
	fun formatDateDiff(date: Long): String {
		val c = GregorianCalendar()
		c.timeInMillis = date
		val now = GregorianCalendar()
		return formatDateDiff(now, c)
	}
	
	fun formatDateDiff(fromDate: Long, toDate: Long): String {
		val c = GregorianCalendar()
		c.timeInMillis = fromDate
		val now = GregorianCalendar()
		now.timeInMillis = toDate
		return formatDateDiff(now, c)
	}
	
	fun formatDateDiff(fromDate: Calendar, toDate: Calendar): String {
		var future = false
		if (toDate == fromDate) {
			return "alguns milisegundos"
		}
		if (toDate.after(fromDate)) {
			future = true
		}
		val sb = StringBuilder()
		val types = intArrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND)
		val names = arrayOf("ano", "anos", "mês", "meses", "dia", "dias", "hora", "horas", "minuto", "minutos", "segundo", "segundos")
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
			"alguns milisegundos"
		} else sb.toString().trim { it <= ' ' }
	}
}

val TIME_PATTERN = "(([01]\\d|2[0-3]):([0-5]\\d)(:([0-5]\\d))?) ?(am|pm)?".toPattern()
val DATE_PATTERN = "(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.]([0-9]+)".toPattern()
fun String.convertToEpochMillisRelativeToNow(): Long {
	val content = this.toLowerCase()
	val calendar = Calendar.getInstance()
	if (content.contains(":")) { // horário
		val matcher = TIME_PATTERN.matcher(content)

		if (matcher.find()) { // Se encontrar...
			val hour = matcher.group(2).toIntOrNull() ?: 0
			val minute = matcher.group(3).toIntOrNull() ?: 0
			val seconds = try {
				matcher.group(5).toIntOrNull() ?: 0
			} catch (e: IllegalStateException) {
				0
			}

			var meridiem = try {
				matcher.group(6)
			} catch (e: IllegalStateException) {
				null
			}

			// Horários que usam o meridiem
			if (meridiem != null) {
				meridiem = meridiem.replace(".", "").replace(" ", "")
				if (meridiem.equals("pm", true)) { // Se for PM, aumente +12
					calendar[Calendar.HOUR_OF_DAY] = (hour % 12) + 12
				} else { // Se for AM, mantenha do jeito atual
					calendar[Calendar.HOUR_OF_DAY] = (hour % 12)
				}
			} else {
				calendar[Calendar.HOUR_OF_DAY] = hour
			}
			calendar[Calendar.MINUTE] = minute
			calendar[Calendar.SECOND] = seconds
		}
	}

	if (content.contains("/")) { // data
		val matcher = DATE_PATTERN.matcher(content)

		if (matcher.find()) { // Se encontrar...
			val day = matcher.group(1).toIntOrNull() ?: 1
			val month = matcher.group(2).toIntOrNull() ?: 1
			val year = matcher.group(3).toIntOrNull() ?: 1999

			calendar[Calendar.DAY_OF_MONTH] = day
			calendar[Calendar.MONTH] = month - 1
			calendar[Calendar.YEAR] = year
		}
	}

	val yearsMatcher = "([0-9]+) ?(y|a)".toPattern().matcher(content)
	if (yearsMatcher.find()) {
		val addYears = yearsMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.YEAR] += addYears
	}
	val monthMatcher = "([0-9]+) ?(month(s)?|m(e|ê)s(es?))".toPattern().matcher(content)
	if (monthMatcher.find()) {
		val addMonths = monthMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.MONTH] += addMonths
	}
	val weekMatcher = "([0-9]+) ?(w)".toPattern().matcher(content)
	if (weekMatcher.find()) {
		val addWeeks = weekMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.WEEK_OF_YEAR] += addWeeks
	}
	val dayMatcher = "([0-9]+) ?(d)".toPattern().matcher(content)
	if (dayMatcher.find()) {
		val addDays = dayMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.DAY_OF_YEAR] += addDays
	}
	val hourMatcher = "([0-9]+) ?(h)".toPattern().matcher(content)
	if (hourMatcher.find()) {
		val addHours = hourMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.HOUR_OF_DAY] += addHours
	}
	val minuteMatcher = "([0-9]+) ?(m)".toPattern().matcher(content)
	if (minuteMatcher.find()) {
		val addMinutes = minuteMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.MINUTE] += addMinutes
	}
	val secondsMatcher = "([0-9]+) ?(s)".toPattern().matcher(content)
	if (secondsMatcher.find()) {
		val addSeconds = secondsMatcher.group(1).toIntOrNull() ?: 0
		calendar[Calendar.SECOND] += addSeconds
	}

	return calendar.timeInMillis
}
