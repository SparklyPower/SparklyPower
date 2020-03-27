package net.perfectdreams.dreamclubes.dao

import net.perfectdreams.dreamclubes.tables.ClubeMembers
import net.perfectdreams.dreamclubes.tables.Clubes
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class Clube(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<Clube>(Clubes)

    var name by Clubes.name
    var cleanName by Clubes.cleanName
    var shortName by Clubes.shortName
    var cleanShortName by Clubes.cleanShortName
    var ownerId by Clubes.ownerId
    var createdAt by Clubes.createdAt
    var home by ClubeHome optionalReferencedOn Clubes.home
    var maxMembers by Clubes.maxMembers

    fun retrieveMembers(): List<ClubeMember> {
        DreamUtils.assertAsyncThread(true)
        val thiz = this

        return transaction(Databases.databaseNetwork) {
            ClubeMember.find {
                ClubeMembers.clube eq thiz.id
            }.toMutableList()
        }
    }

    fun retrieveMember(player: Player) = retrieveMember(player.uniqueId)

    fun retrieveMember(uniqueId: UUID): ClubeMember? {
        DreamUtils.assertAsyncThread(true)
        val thiz = this

        return transaction(Databases.databaseNetwork) {
            ClubeMember.find {
                ClubeMembers.clube eq thiz.id and (ClubeMembers.id eq uniqueId)
            }.firstOrNull()
        }
    }

    fun canExecute(player: Player, permissionLevel: ClubePermissionLevel) = canExecute(player.uniqueId, permissionLevel)

    fun canExecute(uniqueId: UUID, permissionLevel: ClubePermissionLevel): Boolean {
        val clubeMember = retrieveMember(uniqueId) ?: return false
        return clubeMember.permissionLevel.canExecute(permissionLevel)
    }

    fun sendInfo(message: String) {
        async {
            val members = retrieveMembers()
            val onlineMembers = members.mapNotNull { Bukkit.getPlayer(it.id.value) }
            onlineMembers.forEach { player ->
                player.sendMessage("§5INFO §d§lPantufa §6» §3$message")
            }
        }
    }
}