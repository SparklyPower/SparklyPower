package net.perfectdreams.dreamcore.utils

import org.bukkit.ChatColor
import java.text.DecimalFormat

object TextUtils {
	val ROUND_TO_2_DECIMAL = DecimalFormat("##.##")
	val ROUND_TO_1_DECIMAL = DecimalFormat("##.##")

	fun String.stripCodeMarks(): String {
		return this.replace("`", "")
	}

	fun Int.convertToNumeroNomeAdjetivo() = when (this) {
			1 -> "Primeiro"
			2 -> "Segundo"
			3 -> "Terceiro"
			4 -> "Quarto"
			5 -> "Quinto"
			6 -> "Sexto"
			7 -> "S\u00e9timo"
			8 -> "Oitavo"
			9 -> "Nono"
			10 -> "D\u00e9cimo"
			11 -> "D\u00e9cimo primeiro"
			12 -> "D\u00e9cimo segundo"
			13 -> "D\u00e9cimo terceiro"
			14 -> "D\u00e9cimo quarto"
			15 -> "D\u00e9cimo quinto"
			16 -> "D\u00e9cimo sexto"
			17 -> "D\u00e9cimo s\u00e9timo"
			18 -> "D\u00e9cimo oitavo"
			19 -> "D\u00e9cimo nono"
			20 -> "Vig\u00e9simo"
			21 -> "Vig\u00e9simo primeiro"
			22 -> "Vig\u00e9simo segundo"
			23 -> "Vig\u00e9simo terceiro"
			24 -> "Vig\u00e9simo quarto"
			25 -> "Vig\u00e9simo quinto"
			26 -> "Vig\u00e9simo sexto"
			27 -> "Vig\u00e9simo s\u00e9timo"
			28 -> "Vig\u00e9simo oitavo"
			29 -> "Vig\u00e9simo nono"
			30 -> "Trig\u00e9simo"
			31 -> "Trig\u00e9simo primeiro"
			32 -> "Trig\u00e9simo segundo"
			33 -> "Trig\u00e9simo terceiro"
			34 -> "Trig\u00e9simo quarto"
			35 -> "Trig\u00e9simo quinto"
			36 -> "Trig\u00e9simo sexto"
			37 -> "Trig\u00e9simo s\u00e9timo"
			38 -> "Trig\u00e9simo oitavo"
			39 -> "Trig\u00e9simo nono"
			40 -> "Quadrag\u00e9simo"
			41 -> "Quadrag\u00e9simo primeiro"
			42 -> "Quadrag\u00e9simo segundo"
			43 -> "Quadrag\u00e9simo terceiro"
			44 -> "Quadrag\u00e9simo quarto"
			45 -> "Quadrag\u00e9simo quinto"
			46 -> "Quadrag\u00e9simo sexto"
			47 -> "Quadrag\u00e9simo s\u00e9timo"
			48 -> "Quadrag\u00e9simo oitavo"
			49 -> "Quadrag\u00e9simo nono"
			50 -> "Quinquag\u00e9simo"
			51 -> "Quinquag\u00e9simo primeiro"
			52 -> "Quinquag\u00e9simo segundo"
			53 -> "Quinquag\u00e9simo terceiro"
			54 -> "Quinquag\u00e9simo quarto"
			55 -> "Quinquag\u00e9simo quinto"
			56 -> "Quinquag\u00e9simo sexto"
			57 -> "Quinquag\u00e9simo s\u00e9timo"
			58 -> "Quinquag\u00e9simo oitavo"
			59 -> "Quinquag\u00e9simo nono"
			60 -> "Sexag\u00e9simo"
			61 -> "Sexag\u00e9simo primeiro"
			62 -> "Sexag\u00e9simo segundo"
			63 -> "Sexag\u00e9simo terceiro"
			64 -> "Sexag\u00e9simo quarto"
			65 -> "Sexag\u00e9simo quinto"
			66 -> "Sexag\u00e9simo sexto"
			67 -> "Sexag\u00e9simo s\u00e9timo"
			68 -> "Sexag\u00e9simo oitavo"
			69 -> "Sexag\u00e9simo nono"
			70 -> "Septuag\u00e9simo"
			71 -> "Septuag\u00e9simo primeiro"
			72 -> "Septuag\u00e9simo segundo"
			73 -> "Septuag\u00e9simo terceiro"
			74 -> "Septuag\u00e9simo quarto"
			75 -> "Septuag\u00e9simo quinto"
			76 -> "Septuag\u00e9simo sexto"
			77 -> "Septuag\u00e9simo s\u00e9timo"
			78 -> "Septuag\u00e9simo oitavo"
			79 -> "Septuag\u00e9simo nono"
			80 -> "Octog\u00e9simo"
			81 -> "Octog\u00e9simo primeiro"
			82 -> "Octog\u00e9simo segundo"
			83 -> "Octog\u00e9simo terceiro"
			84 -> "Octog\u00e9simo quarto"
			85 -> "Octog\u00e9simo quinto"
			86 -> "Octog\u00e9simo sexto"
			87 -> "Octog\u00e9simo s\u00e9timo"
			88 -> "Octog\u00e9simo oitavo"
			89 -> "Octog\u00e9simo nono"
			90 -> "Nonag\u00e9simo"
			91 -> "Nonag\u00e9simo primeiro"
			92 -> "Nonag\u00e9simo segundo"
			93 -> "Nonag\u00e9simo terceiro"
			94 -> "Nonag\u00e9simo quarto"
			95 -> "Nonag\u00e9simo quinto"
			96 -> "Nonag\u00e9simo sexto"
			97 -> "Nonag\u00e9simo s\u00e9timo"
			98 -> "Nonag\u00e9simo oitavo"
			99 -> "Nonag\u00e9simo nono"
			100 -> "Cent\u00e9simo"
			else -> null
		}

	fun getCenteredMessage(message: String): String {
		return getCenteredMessage(message, 154, false)
	}

	fun getCenteredMessage(message: String, CENTER_PX: Int): String {
		return getCenteredMessage(message, CENTER_PX, false)
	}

	fun getCenteredMessage(message: String?, CENTER_PX: Int, spaceOnRight: Boolean): String {
		var message = message
		if (message == null || message == "") {
			return ""
		}
		message = ChatColor.translateAlternateColorCodes('&', message)
		var messagePxSize = 0
		var previousCode = false
		var isBold = false
		for (c in message!!.toCharArray()) {
			if (c == '§') {
				previousCode = true
			} else if (previousCode) {
				previousCode = false
				isBold = c == 'l' || c == 'L'
			} else {
				val dFI = DefaultFontInfo.getDefaultFontInfo(c)
				messagePxSize += if (isBold) dFI.boldLength else dFI.length
				++messagePxSize
			}
		}
		val halvedMessageSize = messagePxSize / 2
		val toCompensate = CENTER_PX - halvedMessageSize
		val spaceLength = DefaultFontInfo.SPACE.length + 1
		var compensated = 0
		val sb = StringBuilder()
		while (compensated < toCompensate) {
			sb.append(" ")
			compensated += spaceLength
		}
		return sb.toString() + message + if (spaceOnRight) sb.toString() else ""
	}

	fun getHeader(message: String?, CENTER_PX: Int): String {
		var message = message
		if (message == null || message == "") {
			return ""
		}
		message = ChatColor.translateAlternateColorCodes('&', message)
		var messagePxSize = 0
		var previousCode = false
		var isBold = false
		for (c in message!!.toCharArray()) {
			if (c == '§') {
				previousCode = true
			} else if (previousCode) {
				previousCode = false
				isBold = c == 'l' || c == 'L'
			} else {
				val dFI = DefaultFontInfo.getDefaultFontInfo(c)
				messagePxSize += if (isBold) dFI.boldLength else dFI.length
				++messagePxSize
			}
		}
		val halvedMessageSize = messagePxSize / 2
		val toCompensate = CENTER_PX - halvedMessageSize
		val spaceLength = DefaultFontInfo.MINUS.length + 1
		var compensated = 0
		val sb = StringBuilder()
		var colorSwitch = false
		while (compensated < toCompensate) {
			sb.append((if (colorSwitch) "§b" else "§3") + "§m-")
			compensated += spaceLength
			colorSwitch = !colorSwitch
		}
		return sb.toString() + message + sb.toString()
	}

	fun getCenteredHeader(message: String): String {
		return getCenteredMessage(getHeader(message, 154))
	}
}
