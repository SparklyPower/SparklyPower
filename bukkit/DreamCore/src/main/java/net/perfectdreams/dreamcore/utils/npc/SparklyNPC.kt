package net.perfectdreams.dreamcore.utils.npc

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Husk
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID

class SparklyNPC(
    val m: SparklyNPCManager,
    val owner: Plugin,
    var name: String,
    val fakePlayerName: String,
    val initialLocation: Location,
    var textures: SkinTexture?,
    // We can't (and shouldn't!) store the entity reference, since the reference may change when the entity is despawned!
    // So we store the unique ID
    val uniqueId: UUID
) {
    internal var onLeftClickCallback: ((Player) -> (Unit))? = null
    internal var onRightClickCallback: ((Player) -> (Unit))? = null
    var lookClose = false
    var location: Location = initialLocation

    /**
     * Gets the NPC entity, this may be null if the entity is unloaded
     */
    fun getEntity() = Bukkit.getEntity(uniqueId)

    /**
     * Callback that will be invoked when clicking the NPC
     */
    fun onClick(callback: ((Player) -> (Unit))?) {
        onLeftClickCallback = callback
        onRightClickCallback = callback
    }

    /**
     * Teleports the NPC to the new [location]
     */
    fun teleport(location: Location) {
        this.location = location

        getAndUpdateEntity()
    }

    /**
     * Deletes the NPC from the world
     */
    fun remove() {
        m.m.logger.info { "Removing NPC ${uniqueId} from the world and from the NPC storage..." }
        m.npcEntities.remove(uniqueId)
        getEntity()?.remove()
        m.deleteFakePlayerName(this)
    }

    /**
     * Sets the player name
     */
    fun setPlayerName(name: String) {
        this.name = name

        m.updateFakePlayerName(this)
    }

    /**
     * Sets the player's skin textures
     */
    fun setPlayerTextures(textures: SkinTexture?) {
        this.textures = textures

        // When changing the texture, we need to hide and unhide the entity for all players, to resend the player list packet
        val bukkitEntity = getEntity() ?: return // Nevermind...
        Bukkit.getOnlinePlayers().forEach {
            it.hideEntity(m.m, bukkitEntity)
            it.showEntity(m.m, bukkitEntity)
        }
    }

    internal fun updateName(scoreboard: Scoreboard) {
        val length = name.length
        val midpoint = length / 2
        val firstHalf = name.substring(0, midpoint)
        val secondHalf = name.substring(midpoint, length)

        val teamName = SparklyNPCManager.getTeamName(uniqueId)
        val t = scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)
        t.prefix = firstHalf
        t.suffix = secondHalf

        // Bukkit.broadcastMessage("First half is: $firstHalf")
        // Bukkit.broadcastMessage("Second half is: $secondHalf")

        // "Identifiers for the entities in this team. For players, this is their username; for other entities, it is their UUID." - wiki.vg
        t.addEntry(fakePlayerName)
    }

    private fun getAndUpdateEntity() {
        val entity = getEntity() ?: return
        updateEntity(entity)
    }

    fun updateEntity(entity: Entity) {
        val husk = entity as? Husk ?: error("Entity ${entity.uniqueId} is not a NPC!")

        husk.teleport(this.location)
    }
}