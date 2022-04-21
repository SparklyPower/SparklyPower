package net.perfectdreams.dreamraffle.utils

import net.perfectdreams.dreamcore.utils.extensions.pluralize

val Long.remainingTime: String get() {
    val seconds = div(1000).toInt()
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> hours.pluralize("hora")
        minutes > 0 -> minutes.pluralize("minuto")
        else -> seconds.pluralize("segundo")
    }
}