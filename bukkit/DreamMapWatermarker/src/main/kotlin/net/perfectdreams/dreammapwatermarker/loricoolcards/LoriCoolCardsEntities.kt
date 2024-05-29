package net.perfectdreams.dreammapwatermarker.loricoolcards

import kotlinx.serialization.Serializable
import java.awt.Color

@Serializable
data class LoriCoolCardsAlbum(
    val id: Long,
    val eventName: String
)

@Serializable
data class LoriCoolCardsFinishedAlbum(
    val id: Long,
    val finishedPosition: Long,
    val finishedAt: Long,
    val album: LoriCoolCardsAlbum
)

@Serializable
data class LoriCoolCardsSticker(
    val id: Long,
    val fancyCardId: String,
    val title: String,
    val rarity: CardRarity,
    val cardFrontImageUrl: String,
    val cardReceivedImageUrl: String
)

// From Loritta, but changed to use Minecraft-related things
enum class CardRarity(
    val fancyName: String,
    val color: Color,
    val emoji: String,
    val itemCustomModelData: Int
) {
    COMMON("Comum", Color(87, 87, 87), "\ue284", 1),
    UNCOMMON("Incomum", Color(75, 160, 50), "\ue285", 2),
    RARE("Raro", Color(32, 138, 225), "\ue286", 3),
    EPIC("Épico", Color(107, 0, 238), "\ue287", 4),
    LEGENDARY("Lendário", Color(255, 134, 25), "\ue288", 5),
    MYTHIC("Especial", Color(191, 0, 0), "\ue289", 6)
}