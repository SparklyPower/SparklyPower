package net.perfectdreams.dreamcorebungee.utils.discord

import com.google.gson.annotations.SerializedName

class DiscordMessage(
        val username: String? = null,
        val content: String?,
        @SerializedName("avatar_url")
        val avatar: String? = null
)

class DiscordResponse(
        val global: Boolean,
        val message: String,
        @SerializedName("retry_after")
        val retryAfter: Int
)