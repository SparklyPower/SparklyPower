package net.perfectdreams.dreamauth.dao

import net.perfectdreams.dreamauth.tables.AuthStorage
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

class AuthInfo(id: EntityID<UUID>) : UUIDEntity(id) {
	companion object : UUIDEntityClass<AuthInfo>(AuthStorage)

	var password by AuthStorage.password
	var lastIp by AuthStorage.lastIp
	var lastLogin by AuthStorage.lastLogin
	var remember by AuthStorage.remember
	var twoFactorAuthEnabled by AuthStorage.twoFactorAuthEnabled
	var twoFactorAuthSecret by AuthStorage.twoFactorAuthSecret
	var email by AuthStorage.email
	var requestedPasswordChangeAt by AuthStorage.requestedPasswordChangeAt
	var token by AuthStorage.token
}