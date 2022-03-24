package net.perfectdreams.dreamcore.utils.collections

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Returns a new [MutableMap] that has [Player] as its key type.
 * It automatically removes the player from it whenever they quit the game
 * and invokes the defined [callback].
 */
@Suppress("UNCHECKED_CAST")
fun <V : Any> mutablePlayerMapOf(callback: (Player, V) -> Unit = { _, _ -> }) = mutableMapOf<Player, V>().apply {
    playerMaps.add(MapAndCallback(this, callback as (Player, Any) -> Unit))
}
/**
 * Returns a new [MutableMap] that has [Player] as its key type with the given elements.
 * It automatically removes the player from it whenever they quit the game
 * and invokes the defined [callback].
 */
fun <V : Any> mutablePlayerMapOf(vararg pairs: Pair<Player, V>, callback: (Player, V) -> Unit = { _, _ -> }) = mutablePlayerMapOf<V>(callback).apply { putAll(pairs) }

/**
 * Returns a new [MutableList] that has [Player] as its element type.
 * It automatically removes the player from it whenever they quit the game
 * and invokes the defined [callback].
 */
fun mutablePlayerListOf(callback: (Player) -> Unit = {}) = mutableListOf<Player>().apply { playerCollections.add(CollectionAndCallback(this, callback)) }
/**
 * Returns a new [MutableList] that has [Player] as its element type with the given elements.
 * It automatically removes the player from it whenever they quit the game
 * and invokes the defined [callback].
 */
fun mutablePlayerListOf(vararg players: Player, callback: (Player) -> Unit = {}) = mutablePlayerListOf(callback).apply { addAll(players) }

/**
 * Returns a new [MutableSet] that has [Player] as its element type.
 * It automatically removes the player from it whenever they quit the game
 * and invokes the defined [callback].
 */
fun mutablePlayerSetOf(callback: (Player) -> Unit = {}) = mutableSetOf<Player>().apply { playerCollections.add(CollectionAndCallback(this, callback)) }
/**
 * Returns a new [MutableSet] that has [Player] as its element type with the given elements.
 * It automatically removes the player from it whenever they quit the game
 * and invokes the defined [callback].
 */
fun mutablePlayerSetOf(vararg players: Player, callback: (Player) -> Unit = {}) = mutablePlayerSetOf(callback).apply { addAll(players) }

class MapAndCallback(val map: MutableMap<Player, *>, val callback: (Player, Any) -> Unit)

val playerMaps = mutableSetOf<MapAndCallback>().also { set ->
    DreamCore.INSTANCE.registerEvents(
        object : Listener {
            @EventHandler
            fun onQuit(event: PlayerQuitEvent) =
                with (event.player) {
                    set.forEach { it.map.remove(this)?.let { v -> it.callback.invoke(this, v) } }
                }
        }
    )
}

class CollectionAndCallback(val collection: MutableCollection<Player>, val callback: (Player) -> Unit)

val playerCollections = mutableSetOf<CollectionAndCallback>().also { set ->
    DreamCore.INSTANCE.registerEvents(
        object : Listener {
            @EventHandler
            fun onQuit(event: PlayerQuitEvent) =
                with (event.player) {
                    set.forEach { if (it.collection.remove(this)) it.callback.invoke(this) }
                }
        }
    )
}