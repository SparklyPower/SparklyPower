package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object AuthStorage : IdTable<UUID>() {
    override val id: Column<EntityID<UUID>>
        get() = uniqueId

    val uniqueId = uuid("id").entityId()
    val password = varchar("password", 60)
    val lastIp = text("last_ip")
    val lastLogin = long("last_login")
    val remember = bool("remember")
    val twoFactorAuthEnabled = bool("two_factor_auth_enabled")
    val twoFactorAuthSecret = text("two_factor_auth_secret").nullable()
    val email = text("email").nullable()
    val requestedPasswordChangeAt = long("requested_password_change_at").nullable()
    val token = text("token").nullable()

    override val primaryKey = PrimaryKey(uniqueId)
}