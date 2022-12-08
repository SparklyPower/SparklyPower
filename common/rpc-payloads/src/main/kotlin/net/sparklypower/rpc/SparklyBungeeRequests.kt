package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
sealed interface SparklyBungeeRequest {
    @Serializable
    class GetOnlinePlayersRequest : SparklyBungeeRequest

    @Serializable
    class TransferPlayersRequest(
        val playerIds: List<String>,
        val transferTarget: TransferTarget
    ) : SparklyBungeeRequest {
        @Serializable
        sealed class TransferTarget {
            @Serializable
            data class BungeeServerNameTarget(val name: String) : TransferTarget()

            @Serializable
            data class BungeeServerAddressTarget(val name: String, val ip: String, val port: Int) : TransferTarget()
        }
    }
}