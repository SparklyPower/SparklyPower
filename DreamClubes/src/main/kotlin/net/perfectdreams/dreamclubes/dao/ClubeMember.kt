package net.perfectdreams.dreamclubes.dao

import net.perfectdreams.dreamclubes.tables.ClubeMembers
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import org.jetbrains.exposed.dao.*
import java.util.*

class ClubeMember(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ClubeMember>(ClubeMembers)

    var clube by Clube referencedOn ClubeMembers.clube
    var permissionLevel by ClubeMembers.permissionLevel
    var customPrefix by ClubeMembers.customPrefix

    fun canExecute(permissionLevel: ClubePermissionLevel) = this.permissionLevel.canExecute(permissionLevel)
}