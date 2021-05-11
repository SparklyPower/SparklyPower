package net.perfectdreams.dreamcore.utils.discord.parallax

import com.google.gson.annotations.SerializedName

class ParallaxEmbed {
    companion object {
        const val INVALID_IMAGE_URL = "https://loritta.website/assets/img/oopsie_woopsie_invalid_image.png"
    }

    var rgb: ParallaxColor? = null
    var color: Int? = null
    var hex: String? = null
    var title: String? = null
    var url: String?  = null
    var description: String?  = null
    var author: ParallaxEmbedAuthor?  = null
    var thumbnail: ParallaxEmbedImage?  = null
    var image: ParallaxEmbedImage? = null
    var footer: ParallaxEmbedFooter? = null
    var fields: MutableList<ParallaxEmbedField>? = null

    class ParallaxEmbedAuthor(
            var name: String?,
            var url: String?,
            @SerializedName("icon_url")
            var iconUrl: String?
    )

    class ParallaxEmbedImage(
            var url: String?
    )

    class ParallaxEmbedFooter(
            var text: String?,
            @SerializedName("icon_url")
            var iconUrl: String?
    )

    class ParallaxEmbedField(
            var name: String?,
            var value: String?,
            var inline: Boolean = false
    )
}