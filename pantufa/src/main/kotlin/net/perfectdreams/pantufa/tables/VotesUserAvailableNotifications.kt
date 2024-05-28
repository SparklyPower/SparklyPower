package net.perfectdreams.pantufa.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object VotesUserAvailableNotifications : LongIdTable() {
    val userId = uuid("user")
    val botVote = reference("bot_vote", Votes)
    val serviceName = text("service_name")
    val notifyAt = timestampWithTimeZone("notify_at").index()
    val notified = bool("notified").index()
}