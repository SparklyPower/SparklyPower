package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
sealed interface SparklyBungeeResponse {
    @Serializable
    class GetOnlinePlayersResponse(
        val servers: Map<String, List<ProxyPlayer>>
    ) : SparklyBungeeResponse {
        @Serializable
        data class ProxyPlayer(
            val name: String,
            val locale: String,
            val ping: Int,
            val isForgeUser: Boolean
        )
    }

    @Serializable
    sealed interface TransferPlayersResponse : SparklyBungeeResponse {
        @Serializable
        data class Success(val playersNotFoundIds: List<String>) : TransferPlayersResponse
    }
}