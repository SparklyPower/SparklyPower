package net.perfectdreams.pantufa.dao

import net.perfectdreams.pantufa.tables.DiscordAccounts
import org.jetbrains.exposed.dao.*
import java.util.*
import org.jetbrains.exposed.dao.id.EntityID

class DiscordAccount(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DiscordAccount>(DiscordAccounts)

    var minecraftId by DiscordAccounts.minecraftId
    var discordId by DiscordAccounts.discordId
    var isConnected by DiscordAccounts.isConnected
}