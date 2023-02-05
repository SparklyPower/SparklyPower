package net.perfectdreams.dreamsocial.commands.announce.helper

import net.perfectdreams.dreamcore.utils.extensions.changeSaturation
import net.perfectdreams.dreamcore.utils.extensions.toMinecraftColor
import java.awt.Color
import kotlin.random.Random

object PrefixGenerator {
    private const val INITIAL_CHAR = '\uE272' // Allay

    private val colors = listOf(
        0x2FA6EB, // Allay
        0xEBCB2F, // Bee
        0xEB772F, // Fox
        0x22CD0B, // Guardian
        0xF94A36, // Mushroom cow
        0xFB64B5, // Pig
        0xEBAC2f, // Puffer fish
        0x14EB6E, // Slime
        0xDF3932  // Strider
    )
        .map { Color(it) }
        .map {
            Triple(
                it.toMinecraftColor(),
                it.changeSaturation(.6F).toMinecraftColor(),
                it.changeSaturation(.3F).toMinecraftColor()
            )
        }

    private var lastIndex = -1

    private val randomIndex get(): Int = Random.nextInt(9).let {
        if (it == lastIndex) randomIndex
        else it.apply { lastIndex = this }
    }

    fun getPrefixAndColors(): PrefixAndColors = randomIndex.let {
        val colorGroup = colors[it]
        val charPrefix = INITIAL_CHAR + it

        return PrefixAndColors(
            "$charPrefix ${colorGroup.first}§lANÚNCIO§r",
            colorGroup.second,
            colorGroup.third,
        )
    }
}

data class PrefixAndColors(
    val coloredPrefix: String,
    val nicknameColor: String,
    val messageColor: String
)