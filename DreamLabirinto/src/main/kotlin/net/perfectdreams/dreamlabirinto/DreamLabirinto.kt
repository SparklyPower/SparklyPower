package net.perfectdreams.dreamlabirinto

import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamlabirinto.commands.DreamLabirintoCommand
import net.perfectdreams.dreamlabirinto.commands.DreamLabirintoStartCommand
import net.perfectdreams.dreamlabirinto.commands.LabirintoCommand
import net.perfectdreams.dreamlabirinto.events.EventoLabirinto
import net.perfectdreams.dreamlabirinto.listeners.TagListener
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import java.io.File
import java.util.*

class DreamLabirinto : KotlinPlugin(), Listener {
    val event = EventoLabirinto(this)
    lateinit var config: Config

    companion object {
        const val PREFIX = "§8[§e§lLabirinto§8]§e"
    }

    override fun softEnable() {
        super.softEnable()

        this.dataFolder.mkdirs()

        val file = File(this.dataFolder, "config.json")
        if (!file.exists()) {
            file.writeText(
                DreamUtils.gson.toJson(
                    Config(UUID.randomUUID())
                )
            )
        }

        config = DreamUtils.gson.fromJson(file.readText(), Config::class.java)

        registerCommand(DreamLabirintoCommand)
        registerCommand(DreamLabirintoStartCommand)
        registerCommand(LabirintoCommand)

        registerEvents(this)
        registerEvents(TagListener(this))

        registerServerEvent(event)
    }

    override fun softDisable() {
        super.softDisable()

        File(this.dataFolder, "config.json").writeText(DreamUtils.gson.toJson(config))
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (!e.displaced)
            return

        if (!event.running)
            return

        if (e.player.world.name != "Labirinto")
            return

        if (e.to.block.getRelative(BlockFace.DOWN).type == Material.EMERALD_BLOCK)
            event.finish(e.player)
    }

    class Config(var winner: UUID)
}