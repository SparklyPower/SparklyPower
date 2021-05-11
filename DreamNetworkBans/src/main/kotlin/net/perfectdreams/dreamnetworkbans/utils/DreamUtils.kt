package net.perfectdreams.dreamnetworkbans.utils

import net.perfectdreams.dreamcorebungee.dao.User
import net.perfectdreams.dreamcorebungee.tables.Users
import net.perfectdreams.dreamcorebungee.utils.Databases
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object DreamUtils {
    /**
     * Retrieves the user info for the specified [uuid]
     *
     * @param uuid the user's unique ID
     * @return the user's data, if present
     */
    fun retrieveUserInfo(uuid: UUID): User? {
        return transaction(Databases.databaseNetwork) {
            User.findById(uuid)
        }
    }

    /**
     * Retrieves the user info for the specified [playerName]
     *
     * @param playerName user's name
     * @return the user's data, if present
     */
    fun retrieveUserInfo(playerName: String): User? {
        return transaction(Databases.databaseNetwork) {
            User.find { Users.username eq playerName }
                    .firstOrNull()
        }
    }

    /**
     * Retrieves the player's UUID for the specified [playerName]
     * If the player does not exist on the database, it will default
     * to "OfflinePlayer:$playerName"
     *
     * @param playerName user's name
     * @return the user's data, if present
     */
    fun retrieveUserUniqueId(playerName: String): UUID {
        return retrieveUserInfo(playerName)?.id?.value ?: UUID.nameUUIDFromBytes("OfflinePlayer:$playerName".toByteArray(Charsets.UTF_8))
    }
}