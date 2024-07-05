package net.sparklypower.rpc

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class TransferSonecasRequest(
    val giverName: String,
    val requestedById: String,
    val receiverName: String,
    val quantity: Double,
    val bypassLastActiveTime: Boolean
)

@Serializable
sealed class TransferSonecasResponse {
    @Serializable
    data object CannotTransferSonecasToSelf : TransferSonecasResponse()

    @Serializable
    data object PlayerHasNotJoinedRecently : TransferSonecasResponse()

    @Serializable
    data object UserDoesNotExist : TransferSonecasResponse()

    @Serializable
    data class NotEnoughSonecas(val currentUserMoney: Double) : TransferSonecasResponse()

    @Serializable
    data class Success(
        val receiverName: String,
        val receiverId: String,
        val quantityGiven: Double,
        val selfMoney: Double,
        val selfRanking: Long,
        val receiverMoney: Double,
        val receiverRanking: Long
    ) : TransferSonecasResponse()
}