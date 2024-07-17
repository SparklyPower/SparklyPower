package net.perfectdreams.pantufa.utils

import net.perfectdreams.loritta.common.emotes.DiscordEmote

object Emotes {
    val DefaultStyledPrefix = UnicodeEmote("\uD83D\uDD39")
    val MoneyWithWings = UnicodeEmote("\uD83D\uDCB8")
    val DollarBill = UnicodeEmote("\uD83D\uDCB5")
    val MoneyBag = UnicodeEmote("\uD83D\uDCB0")
    val CreditCard = UnicodeEmote("\uD83D\uDCB3")
    val Envelope = UnicodeEmote("\uD83D\uDCE9")
    val Tickets = UnicodeEmote("\uD83C\uDF9F")
    val Dart = UnicodeEmote("\uD83C\uDFAF")
    val Gift = UnicodeEmote("\uD83C\uDF81")

    val PantufaCoffee = PantufaEmoji(
        853048446981111828,
        "pantufa_coffee",
        false
    )
    val PantufaBonk = PantufaEmoji(
        1028160322990776331,
        "pantufa_bonk",
        false
    )
    val PantufaClown = PantufaEmoji(
        1004449971342426186,
        "pantufa_clown",
        false
    )
    val PantufaSmart = PantufaEmoji(
        997671151587299348,
        "pantufa_smart",
        false
    )
    val PantufaSob = PantufaEmoji(
        1009905676925026325,
        "pantufa_sob",
        false
    )
    val PantufaThinking = PantufaEmoji(
        853048446813470762,
        "pantufa_analise",
        false
    )
    val PantufaComfy = PantufaEmoji(
        853048447254396978,
        "pantufa_comfy",
        false
    )
    val PantufaFire = PantufaEmoji(
        1049493744925290536,
        "pantufa_fire",
        false
    )
    val PantufaFlushed = PantufaEmoji(
        853048447212322856,
        "pantufa_flushed",
        false
    )
    val PantufaHangloose = PantufaEmoji(
        982762886105534565,
        "pantufa_hangloose",
        false
    )
    val LoriHanglooseRight = PantufaEmoji(982764105918205952L, "lori_hangloose", false)
    val GabrielaHanglooseRight = PantufaEmoji(982764945236176919L, "gabi_hangloose", false)
    val PantufaHanglooseRight = PantufaEmoji(1008059742356250759L, "pantufa_hangloose_right", false)
    val PowerHanglooseRight = PantufaEmoji(1008059744717635624L, "power_hangloose_right", false)
    val PantufaHeart = PantufaEmoji(
        853048447175098388,
        "pantufa_heart",
        false
    )
    val PantufaHi = PantufaEmoji(
        997662575779139615,
        "pantufa_hi",
        false
    )
    val PantufaLick = PantufaEmoji(
        958906311414796348,
        "pantufa_lick",
        true
    )
    val PantufaLurk = PantufaEmoji(
        1012841674856214528,
        "pantufa_lurk",
        false
    )
    val PantufaMegaphone = PantufaEmoji(
        997669904633299014,
        "pantufa_megaphone",
        false
    )
    val PantufaOk = PantufaEmoji(
        853048447232901130,
        "pantufa_ok",
        false
    )
    val PantufaOverThinking = PantufaEmoji(
        1100956303645487175,
        "pantufa_pensando_muito",
        true
    )
    val PantufaPickaxe = PantufaEmoji(
        997671670468853770,
        "pantufa_pickaxe",
        true
    )
    val PantufaThumbsUp = PantufaEmoji(
        853048446826840104,
        "pantufa_thumbsup",
        false
    )
    val PantufaShrug = PantufaEmoji(
        1004449909816168489,
        "pantufa_shrug",
        false
    )
    val Pesadelos = PantufaEmoji(
        983023709369536582L,
        "pesadelos",
        false
    )
    val Sonecas = PantufaEmoji(
        983023797877743698L,
        "sonecas",
        false
    )
    val FilledMap = PantufaEmoji(
        1254988625511186463L,
        "filled_map",
        false
    )

    sealed class Emote {
        /**
         * The emote name
         */
        abstract val name: String

        abstract val asMention: String
    }

    data class PantufaEmoji(
        val id: Long,
        override val name: String,
        val animated: Boolean
    ) : Emote() {
        override val asMention: String
            get() = "<${if (animated) "a" else ""}:$name:$id>"

        override fun toString(): String {
            return this.asMention
        }
    }

    class UnicodeEmote(override val name: String) : Emote() {
        override val asMention: String
            get() = name

        override fun toString() = asMention
    }
}