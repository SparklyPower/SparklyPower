package net.perfectdreams.pantufa.utils.config

import kotlinx.serialization.Serializable

@Serializable
class DiscordInteractionsConfig(
    val publicKey: String,
    val registerGlobally: Boolean,
    val guildsToBeRegistered: List<Long>
)