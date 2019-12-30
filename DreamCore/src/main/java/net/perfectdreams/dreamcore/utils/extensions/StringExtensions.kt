package net.perfectdreams.dreamcore.utils.extensions

import net.perfectdreams.dreamcore.utils.TextUtils

fun String.centralize() = TextUtils.getCenteredMessage(this)
fun String.centralizeHeader() = TextUtils.getCenteredHeader(this)