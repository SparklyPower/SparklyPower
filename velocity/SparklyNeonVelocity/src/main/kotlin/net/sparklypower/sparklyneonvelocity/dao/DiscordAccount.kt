package net.sparklypower.sparklyneonvelocity.dao

import net.sparklypower.sparklyneonvelocity.tables.DiscordAccounts
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class DiscordAccount(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DiscordAccount>(DiscordAccounts)

    var minecraftId by DiscordAccounts.minecraftId
    var discordId by DiscordAccounts.discordId
    var isConnected by DiscordAccounts.isConnected
}