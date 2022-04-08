package net.perfectdreams.dreamcore.utils.extensions

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

private val formatter = NumberFormat.getNumberInstance(Locale.GERMAN) as DecimalFormat

val Int.formatted: String get() = toLong().formatted

val Long.formatted: String get() = formatter.format(this)

fun Int.pluralize(options: Pair<String, String>) = toLong().pluralize(options)

fun Long.pluralize(options: Pair<String, String>) =
    "$formatted ${if (equals(1L)) options.first else options.second}"

val Double.percentage get() = "%.2f".format(times(100)) + "%"