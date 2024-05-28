package net.perfectdreams.pantufa.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object SurvivalTrackedOnlineHours : LongIdTable() {
    override val tableName: String
        get() = "survival_trackedonlinehours"

    val player = uuid("player").index()
    val loggedIn = timestampWithTimeZone("logged_in").index()
    val loggedOut = timestampWithTimeZone("logged_out")
}