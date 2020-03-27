package net.perfectdreams.dreamclubes.utils

import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.tables.ClubeMembers
import net.perfectdreams.dreamclubes.tables.Clubes
import net.perfectdreams.dreamclubes.utils.ClubeNameCheckResult.*
import net.perfectdreams.dreamcore.utils.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ClubeAPI {
    fun getPlayerKD(uniqueId: UUID): KDWrapper {
        return KDWrapper(0, 0)
    }

    fun checkIfClubeCanUseTag(player: Player, clube: Clube?, tag: String): ClubeNameCheckResult {
        DreamUtils.assertAsyncThread(true)

        val cleanTag = tag.translateColorCodes().stripColorCode()

        if (cleanTag.toLowerCase() in arrayOf("mod", "ppk", "adm", "admin", "moder"))
            return BLACKLISTED_NAME

        if (cleanTag.isEmpty())
            return TAG_TOO_SHORT

        if (player.hasPermission("dreamclubes.longtags")) {
            if (cleanTag.length > 5)
                return TAG_TOO_LONG_VIP
        } else {
            if (cleanTag.length > 3)
                return TAG_TOO_LONG
        }

        val clubeByCleanTag = getClubeByCleanTag(cleanTag)
        clubeByCleanTag?.let {
            if (it.id != clube?.id)
                return ALREADY_IN_USE
        }

        return OK
    }

    fun checkIfClubeCanUseTagAndSendMessages(player: Player, clube: Clube?, tag: String): Boolean {
        val checkResult = checkIfClubeCanUseTag(player, clube, tag)

        when (checkResult) {
            ALREADY_IN_USE -> {
                player.sendMessage("${DreamClubes.PREFIX} §cJá existe outro clube utilizando a tag! Não tente kibar tags de outros clubes ;)")
                return false
            }
            BLACKLISTED_NAME -> {
                player.sendMessage("${DreamClubes.PREFIX} §cA tag que você tentou utilizar está bloqueada!")
                return false
            }
            TAG_TOO_SHORT -> {
                player.sendMessage("${DreamClubes.PREFIX} §cA tag que você tentou utilizar é pequena demais!")
                return false
            }
            TAG_TOO_LONG -> {
                player.sendMessage("${DreamClubes.PREFIX} §cA tag que você tentou utilizar é longa demais, o máximo que você pode utilizar é 3 caracteres. Para poder criar clans com tags de até 5 caracteres, tenha VIP++!")
                return false
            }
            TAG_TOO_LONG_VIP -> {
                player.sendMessage("${DreamClubes.PREFIX} §cA tag que você tentou utilizar é longa demais, o máximo que você pode utilizar é 5 caracteres.")
                return false
            }
            OK -> return true
        }
    }

    fun getClubePlayerWrapper(playerId: UUID): ClubeMember? {
        DreamUtils.assertAsyncThread(true)

        return transaction(Databases.databaseNetwork) {
            ClubeMember.find {
                ClubeMembers.id eq playerId
            }.firstOrNull()
        }
    }

    fun getOrCreateClubePlayerWrapper(playerId: UUID, clube: Clube): ClubeMember {
        DreamUtils.assertAsyncThread(true)

        return transaction(Databases.databaseNetwork) {
            ClubeMember.find {
                ClubeMembers.id eq playerId
            }.firstOrNull() ?: ClubeMember.new(playerId) {
                this.clube = clube
                this.permissionLevel = ClubePermissionLevel.MEMBER
            }
        }
    }

    fun getClubeByCleanTag(cleanTag: String): Clube? {
        DreamUtils.assertAsyncThread(true)

        return transaction(Databases.databaseNetwork) {
            Clube.find {
                Clubes.cleanShortName eq cleanTag
            }.firstOrNull()
        }
    }

    fun getClubeByTag(tag: String): Clube? {
        DreamUtils.assertAsyncThread(true)

        return transaction(Databases.databaseNetwork) {
            Clube.find {
                Clubes.shortName eq tag
            }.firstOrNull()
        }
    }

    fun getPlayerClube(player: Player) = getPlayerClube(player.uniqueId)

    fun getPlayerClube(playerId: UUID) = transaction(Databases.databaseNetwork) { getClubePlayerWrapper(playerId)?.clube }
}

fun async(callback: suspend com.okkero.skedule.BukkitSchedulerController.() -> kotlin.Unit) {
    scheduler().schedule(Bukkit.getPluginManager().getPlugin("DreamClubes"), SynchronizationContext.ASYNC, callback)
}

fun sync(callback: suspend com.okkero.skedule.BukkitSchedulerController.() -> kotlin.Unit) {
    scheduler().schedule(Bukkit.getPluginManager().getPlugin("DreamClubes"), SynchronizationContext.SYNC, callback)
}

suspend fun BukkitSchedulerController.toAsync() {
    switchContext(SynchronizationContext.ASYNC)
}

suspend fun BukkitSchedulerController.toSync() {
    switchContext(SynchronizationContext.SYNC)
}