package net.perfectdreams.pantufa.rpc

import kotlinx.serialization.Serializable

@Serializable
sealed interface PantufaRPCRequest

@Serializable
class GetDiscordUserRequest(val userId: Long) : PantufaRPCRequest