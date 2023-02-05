package net.perfectdreams.dreamsocial.gui.profile.helper.item

fun getPlayerAnniversaryCustomModelData(numberOfYears: Long) = when (numberOfYears) {
    1L -> 91
    2L -> 92
    3L -> 93
    4L -> 94
    5L -> 95
    else -> 96
}