package net.perfectdreams.dreamraffle.raffle

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.perfectdreams.dreamcore.utils.serializer.UUIDAsStringSerializer
import net.perfectdreams.dreamraffle.utils.remainingTime
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

    val winner get() =
        with (winnerUUID) {
            Winner(this, getTickets(this).toDouble() / tickets, tickets, type)
        }

    val prefix = "${type.colors.default} ⤖"
    var hasNotified = false
    val shouldNotify get() = (end - System.currentTimeMillis() <= 60_000) && !hasNotified
    val remainingTime get() = (end - System.currentTimeMillis()).remainingTime
    val shouldEnd get() = System.currentTimeMillis() > end
    val start = System.currentTimeMillis()
    val end = start + type.duration.toMillis()
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
data class Winner(
    @Serializable(with = UUIDAsStringSerializer::class) val uuid: UUID,
    @Transient var chance: Double = 0.0,
    val raffleTickets: Long,
    val type: RaffleType
)