package net.perfectdreams.dreamcore.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object DiscordAccounts : LongIdTable() {
    val minecraftId = uuid("minecraft_id").index()
    val discordId = long("discord_id").index()
    val isConnected = bool("is_connected").index()
}