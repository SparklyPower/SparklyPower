@file:Suppress("UNUSED")
package net.perfectdreams.dreamcore.utils.preferences

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.dao.PreferencesEntity
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object PreferencesManager {
    private val preferences = mutablePlayerMapOf<PreferencesEntity>()
    private val plugin = DreamCore.INSTANCE

    init {
        plugin.registerEvents(object : Listener {
            @EventHandler
            fun onJoin(event: PlayerJoinEvent) {
                plugin.schedule(SynchronizationContext.ASYNC) {
                    preferences[event.player] = PreferencesEntity.fetch(event.player.uniqueId)
                }
            }
        })
    }

    fun getPlayerPreferences(player: Player) = preferences[player]
}