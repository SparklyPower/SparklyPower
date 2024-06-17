package net.perfectdreams.pantufa.utils.extensions

fun String.normalize(): String {
    val replacements = mapOf('ę' to 'e', 'š' to 's')

    return this.map { replacements[it] ?: it }.joinToString("")
}