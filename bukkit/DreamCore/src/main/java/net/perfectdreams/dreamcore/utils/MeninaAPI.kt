package net.perfectdreams.dreamcore.utils

import net.perfectdreams.dreamcore.dao.User
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object MeninaAPI {
    const val PREFIX_GIRL = "§8[§d§lMenina§8]§e "
    const val PREFIX_BOY = "§8[§b§lMenino§8]§e "

    var girls = mutableSetOf<UUID>()

    fun isGirl(uniqueId: UUID): Boolean {
        return girls.contains(uniqueId)
    }

    fun getPronome(player: Player): String {
        return getPronome(player.uniqueId)
    }

    fun getArtigo(player: Player): String {
        return getArtigo(player.uniqueId)
    }

    fun getPronome(uniqueId: UUID): String {
        return if (isGirl(uniqueId)) { "ela" } else { "ele" }
    }

    fun getArtigo(uniqueId: UUID): String {
        return if (isGirl(uniqueId)) { "a" } else { "o" }
    }

    fun setGirlStatus(uniqueId: UUID, isGirl: Boolean) {
        DreamUtils.assertAsyncThread(true)

        if (isGirl) {
            girls.add(uniqueId)
        } else {
            girls.remove(uniqueId)
        }

        transaction(Databases.databaseNetwork) {
            val user = User.findById(uniqueId) ?: throw RuntimeException("User $uniqueId doesn't have a generated profile! Bug?")
            user.isGirl = isGirl
        }
    }
}