package net.perfectdreams.pantufa.tables

import org.jetbrains.exposed.dao.id.LongIdTable

// Esta table é um long boi
// Ao inserir na tabela, o ID irá incrementar (1 -> 2 -> 3... etc)
object Bans : LongIdTable() {
	// UUID do player
	// Já que a gente vai acessar se o ban existe várias vezes, vamos indexar!
	val player = uuid("player").index()

	// Punido por...
	// Sim, pode ser nulo, caso seja nulo, iremos colocar quem puniu como "Pantufa"
	val punishedBy = uuid("punished_by").nullable()
	val punishedAt = long("punished_at")

	// Motivo da punição
	val reason = text("reason").nullable()
	val temporary = bool("temporary").default(false)
	val expiresAt = long("expires_at").nullable()
}
