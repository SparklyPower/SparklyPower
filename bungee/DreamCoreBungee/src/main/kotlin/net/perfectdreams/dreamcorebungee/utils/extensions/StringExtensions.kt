package net.perfectdreams.dreamcorebungee.utils.extensions

import net.perfectdreams.dreamcorebungee.utils.TextUtils

fun String.centralize() = TextUtils.getCenteredMessage(this)
fun String.centralizeHeader() = TextUtils.getCenteredHeader(this)