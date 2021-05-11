package net.perfectdreams.dreamnetworkbans.tables

import net.md_5.bungee.api.connection.ProxiedPlayer
import org.jetbrains.exposed.dao.LongIdTable

// Tabela de fingerprints, serve para guardar informações sobre players que podem ser usadas para ser identificadas mesmo se mudar de conta!
object Fingerprints : LongIdTable() {
	// UUID do player
	val player = uuid("player").index()
	val createdAt = long("created_at")
	val isForgeUser = bool("forge_user")
	val chatMode = enumeration("chat_mode", ProxiedPlayer.ChatMode::class)
	val mainHand = enumeration("main_hand", ProxiedPlayer.MainHand::class)
	val language = text("language")
	val viewDistance = integer("view_distance")
	val version = text("version")

	val hasCape = bool("has_cape")
	val hasHat = bool("has_hat")
	val hasJacket = bool("has_jacket")
	val hasLeftPants = bool("has_left_pants")
	val hasLeftSleeve = bool("has_left_sleeve")
	val hasRightPants = bool("has_right_pants")
	val hasRightSleeve = bool("has_right_sleeve")
}