package net.perfectdreams.dreamcore.utils

import java.util.*
import java.util.regex.Pattern

// Copyright by FisheyLP, Version 1.3 (12.08.16)
class TableGenerator(vararg alignments: Alignment) {
	private val alignments: Array<out Alignment>
	private val table = ArrayList<Row>()
	private val columns: Int

	init {
		if (alignments.isEmpty())
			throw IllegalArgumentException("Must atleast provide 1 alignment.")

		this.columns = alignments.size
		this.alignments = alignments
	}

	fun generate(receiver: Receiver, ignoreColors: Boolean,
				 coloredDistances: Boolean): List<String> {
		val columWidths = arrayOfNulls<Int>(columns)

		for (r in table) {
			for (i in 0 until columns) {
				val text = r.texts[i]
				val length: Int

				if (ignoreColors)
					length = getCustomLength(text.replace(colors.toRegex(), ""),
							receiver)
				else
					length = getCustomLength(text, receiver)

				if (columWidths[i] == null) {
					columWidths[i] = length
				} else if (length > columWidths[i]!!) {
					columWidths[i] = length
				}
			}
		}

		val lines = ArrayList<String>()

		for (r in table) {
			val sb = StringBuilder()

			if (r.empty) {
				lines.add("")
				continue
			}

			for (i in 0 until columns) {
				val agn = alignments[i]
				val text = r.texts[i]
				val length: Int

				if (ignoreColors)
					length = getCustomLength(text.replace(colors.toRegex(), ""),
							receiver)
				else
					length = getCustomLength(text,
							receiver)

				val empty = columWidths[i]!! - length
				var spacesAmount = empty
				if (receiver == Receiver.CLIENT)
					spacesAmount = Math.floor(empty / 4.0).toInt()
				var char1Amount = 0
				if (receiver == Receiver.CLIENT)
					char1Amount = empty - 4 * spacesAmount

				val spaces = concatChars(' ', spacesAmount)
				var char1s = concatChars(char1, char1Amount)

				if (coloredDistances)
					char1s = "§r§8$char1s§r"

				if (agn == Alignment.LEFT) {
					sb.append(text)
					if (i < columns - 1)
						sb.append(char1s).append(spaces)
				}
				if (agn == Alignment.RIGHT) {
					sb.append(spaces).append(char1s).append(text)
				}
				if (agn == Alignment.CENTER) {
					val leftAmount = empty / 2
					val rightAmount = empty - leftAmount

					var spacesLeftAmount = leftAmount
					var spacesRightAmount = rightAmount
					if (receiver == Receiver.CLIENT) {
						spacesLeftAmount = Math.floor(spacesLeftAmount / 4.0).toInt()
						spacesRightAmount = Math.floor(spacesRightAmount / 4.0).toInt()
					}

					var char1LeftAmount = 0
					var char1RightAmount = 0
					if (receiver == Receiver.CLIENT) {
						char1LeftAmount = leftAmount - 4 * spacesLeftAmount
						char1RightAmount = rightAmount - 4 * spacesRightAmount
					}

					val spacesLeft = concatChars(' ', spacesLeftAmount)
					val spacesRight = concatChars(' ', spacesRightAmount)
					var char1Left = concatChars(char1, char1LeftAmount)
					var char1Right = concatChars(char1, char1RightAmount)

					if (coloredDistances) {
						char1Left = "§r§8$char1Left§r"
						char1Right = "§r§8$char1Right§r"
					}

					sb.append(spacesLeft).append(char1Left).append(text)
					if (i < columns - 1)
						sb.append(char1Right).append(spacesRight)
				}

				if (i < columns - 1) sb.append("§r" + delimiter)
			}

			var line = sb.toString()
			if (receiver == Receiver.CLIENT) {
				for (i in 0..1) {
					val matcher = regex.matcher(line)
					line = matcher.replaceAll("$1$2$3 ").replace("§r§8§r", "§r")
							.replace("§r(\\s*)§r".toRegex(), "§r$1")
				}
			}
			lines.add(line)
		}
		return lines
	}

	protected fun concatChars(c: Char, length: Int): String {
		var s = ""
		if (length < 1) return s

		for (i in 0 until length)
			s += Character.toString(c)
		return s
	}

	fun addRow(vararg texts: String) {
		if (texts == null) {
			throw IllegalArgumentException("Texts must not be null.")
		}
		if (texts != null && texts.size > columns) {
			throw IllegalArgumentException("Too big for the table.")
		}

		val r = Row(*texts)

		table.add(r)
	}

	private inner class Row(vararg texts: String) {

		var texts: MutableList<String> = ArrayList()
		var empty = true

		init {
			for (text in texts) {
				if (text != null && !text.isEmpty()) empty = false

				this.texts.add(text)
			}

			for (i in 0 until columns) {
				if (i >= texts.size) this.texts.add("")
			}
		}
	}

	enum class Receiver {

		CONSOLE, CLIENT
	}

	enum class Alignment {

		CENTER, LEFT, RIGHT
	}

	companion object {

		private val delimiter = " "
		private val char7 = Arrays.asList('°', '~', '@')
		private val char5 = Arrays.asList('"', '{', '}', '(', ')', '*', 'f', 'k', '<', '>')
		private val char4 = Arrays.asList('I', 't', ' ', '[', ']', '€')
		private val char3 = Arrays.asList('l', '`', '³', '\'')
		private val char2 = Arrays.asList(',', '.', '!', 'i', '´', ':', ';', '|')
		private val char1 = '\u17f2'
		private val regex = Pattern.compile(char1 + "(?:§r)?(\\s*)"
				+ "(?:§r§8)?" + char1 + "(?:§r)?(\\s*)"
				+ "(?:§r§8)?" + char1 + "(?:§r)?(\\s*)"
				+ "(?:§r§8)?" + char1)
		private val colors = "[&§][0-9a-fA-Fk-oK-OrR]"

		protected fun getCustomLength(text: String?, receiver: Receiver?): Int {
			if (text == null) {
				throw IllegalArgumentException("Text must not be null.")
			}
			if (receiver == null) {
				throw IllegalArgumentException("Receiver must not be null.")
			}
			if (receiver == Receiver.CONSOLE) return text.length

			var length = 0
			for (c in text.toCharArray())
				length += getCustomCharLength(c)

			return length
		}

		protected fun getCustomCharLength(c: Char): Int {
			if (char1 == c) return 1
			if (char2.contains(c)) return 2
			if (char3.contains(c)) return 3
			if (char4.contains(c)) return 4
			if (char5.contains(c)) return 5
			return if (char7.contains(c)) 7 else 6

		}
	}
}