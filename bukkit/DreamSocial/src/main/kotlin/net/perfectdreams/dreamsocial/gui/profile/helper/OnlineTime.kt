package net.perfectdreams.dreamsocial.gui.profile.helper

import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.asComponent
import net.perfectdreams.dreamcore.utils.extensions.pluralize

fun generateOnlineTimeMessage(onlineTime: Long) = textComponent {
    color { 0xffd44e }

    val numberOfDays = onlineTime / 86400
    val numberOfHours = onlineTime % 86400 / 3600
    val numberOfMinutes = onlineTime % 86400 % 3600 / 60

    val components = mutableListOf<String>()

    if (numberOfDays > 0) components.add(numberOfDays.pluralize("dia"))
    if (numberOfHours > 0) components.add(numberOfHours.pluralize("hora"))
    if (numberOfMinutes > 0) components.add(numberOfMinutes.pluralize("minuto"))

    components.forEachIndexed { index, it ->
        if (index == components.size - 1 && components.size > 1) {
            append(" e ")
            append(it.asComponent.color { 0xffe79d })
        }

        else {
            append(it.asComponent.color { 0xffe79d })
            if (index != components.size - 2 && components.size > 1) append(", ")
        }
    }

    append(".")
}