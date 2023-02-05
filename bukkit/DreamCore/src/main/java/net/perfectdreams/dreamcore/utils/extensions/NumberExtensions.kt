package net.perfectdreams.dreamcore.utils.extensions

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

private val formatter = NumberFormat.getNumberInstance(Locale.GERMAN) as DecimalFormat

val Number.formatted: String get() = formatter.format(this)

fun Number.pluralize(word: String, includeNumber: Boolean = true) = this.pluralize(word to "${word}s", includeNumber)

fun Number.pluralize(options: Pair<String, String>, includeNumber: Boolean = true) =
    "${if (includeNumber) "$formatted " else ""}${if (equals(1) || equals(1L) || equals(1.0F) || equals(1)) options.first else options.second}"

val Double.percentage get() = "%.2f".format(times(100)) + "%"