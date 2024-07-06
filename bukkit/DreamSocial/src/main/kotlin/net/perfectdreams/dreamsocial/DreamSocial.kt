@file:Suppress("UNUSED")
package net.perfectdreams.dreamsocial

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamsocial.commands.announce.AnnounceCommand
import net.perfectdreams.dreamsocial.commands.profile.ProfileCommand
import net.perfectdreams.dreamsocial.tables.AnnouncementsTable
import net.perfectdreams.dreamsocial.tables.ProfilesTable
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DreamSocial : KotlinPlugin() {
    override fun softEnable() {
        super.softEnable()

        transaction(Databases.databaseNetwork) {
            SchemaUtils.createMissingTablesAndColumns(AnnouncementsTable, ProfilesTable)
        }

        val dreamChat = (Bukkit.getPluginManager().getPlugin("DreamChat") as DreamChat)

        // Disabled because it is a bit buggy
        registerCommand(ProfileCommand(this))
        registerCommand(AnnounceCommand(this, dreamChat))
    }
}