package net.perfectdreams.dreamnetworkbans.tables

import net.perfectdreams.dreamnetworkbans.utils.LoginConnectionStatus
import org.jetbrains.exposed.dao.id.LongIdTable

// Esta table é um long boi
// Ao inserir na tabela, o ID irá incrementar (1 -> 2 -> 3... etc)
object ConnectionLogEntries : LongIdTable() {
	// UUID do player
	// Já que a gente vai acessar se o ban existe várias vezes, vamos indexar!
	val player = uuid("player").index()
	val ip = text("ip").index()
	val connectedAt = long("connected_at")
	val connectionStatus = enumeration("connection_status", LoginConnectionStatus::class)
}
