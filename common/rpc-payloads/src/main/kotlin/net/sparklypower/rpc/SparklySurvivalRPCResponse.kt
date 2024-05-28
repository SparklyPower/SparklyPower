package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
sealed class SparklySurvivalRPCResponse {
    @Serializable
    sealed class GetSonecasResponse : SparklySurvivalRPCResponse() {
        @Serializable
        class Success(val money: Double, val rankPosition: Long?) : GetSonecasResponse()
    }

    @Serializable
    sealed class TransferSonecasResponse {

    }
}