package net.perfectdreams.dreamraffle.raffle

import kotlinx.serialization.Serializable
import net.perfectdreams.dreamcore.utils.extensions.pluralize
import net.perfectdreams.dreamcore.utils.serializer.UUIDAsStringSerializer
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.random.Random

@Serializable
class Raffle(val type: RaffleType) {
    private val participants = mutableSetOf<Gambler>()
    private val probabilities get() =
        with (participants) {
            groupByTo(sortedMapOf(compareByDescending { it })) {
                with (it.tickets) { (toDouble() / tickets) * count { it.tickets == this } }.toDouble()
            }
        }

    private val winnerUUID get() =
        with (probabilities.entries) {
            var chance = Random.nextDouble()
            forEach {
                if (chance <= it.key) return@with it
                else chance -= it.key
            }
            last()
        }.value.random().uuid

    val winner get() = Winner(winnerUUID, tickets, type)

    val remainingTime get() =
        with(end - System.currentTimeMillis()) {
            val seconds = div(1000).toInt()
            val minutes = seconds / 60

            if (minutes > 0) minutes.pluralize("minuto" to "minutos")
            else seconds.pluralize("segundo" to "segundos")
        }

    val shouldEnd get() = System.currentTimeMillis() > end
    val start = System.currentTimeMillis()
    val end = start + type.expiresIn.toMillis()
    var tickets = 0L

    fun addTickets(player: Player, tickets: Long) = addTickets(player.uniqueId, tickets)
    fun addTickets(uuid: UUID, tickets: Long) = Gambler(uuid, tickets).let { gambler ->
        with (participants) {
            firstOrNull { it == gambler }?.let { it.tickets += tickets } ?: add(gambler)
            this@Raffle.tickets += tickets
        }
    }

    fun getTickets(player: Player) = getTickets(player.uniqueId)
    fun getTickets(uuid: UUID) = participants.firstOrNull { it.uuid == uuid }?.tickets ?: 0
}

@Serializable
data class Gambler(@Serializable(with = UUIDAsStringSerializer::class) val uuid: UUID, var tickets: Long = 0) {
    override fun equals(other: Any?) = hashCode() == other.hashCode()
    override fun hashCode() = uuid.hashCode()
}

@Serializable
data class Winner(@Serializable(with = UUIDAsStringSerializer::class) val uuid: UUID,
                  val raffleTickets: Long, val type: RaffleType)