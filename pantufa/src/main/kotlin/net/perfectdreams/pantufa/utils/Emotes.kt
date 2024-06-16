package net.perfectdreams.pantufa.utils

object Emotes {
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

    data class PantufaEmoji(
        val id: Long,
        val name: String,
        val animated: Boolean
    ) {
        val asMention: String
            get() = "<${if (animated) "a" else ""}:$name:$id>"

        override fun toString(): String {
            return this.asMention
        }
    }
}