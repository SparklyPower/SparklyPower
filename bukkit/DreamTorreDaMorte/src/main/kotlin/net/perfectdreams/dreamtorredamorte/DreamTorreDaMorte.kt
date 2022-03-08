package net.perfectdreams.dreamtorredamorte

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.DreamEventManager
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamtorredamorte.commands.DreamTorreDaMorteStartCommand
import net.perfectdreams.dreamtorredamorte.commands.TorreCommand
import net.perfectdreams.dreamtorredamorte.commands.TorreMinigameCommand
import net.perfectdreams.dreamtorredamorte.event.EventoTorreDaMorte
import net.perfectdreams.dreamtorredamorte.listeners.PlayerListener
import net.perfectdreams.dreamtorredamorte.listeners.TagListener
import net.perfectdreams.dreamtorredamorte.utils.TorreDaMorte
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class DreamTorreDaMorte : KotlinPlugin() {
    companion object {
        const val PREFIX = "§8[§c§lTorre da §4§lMorte§8]§e"
    }

    val torreDaMorte = TorreDaMorte(this)
    val eventoTorreDaMorte = EventoTorreDaMorte(this)

    val dataYaml by lazy {
        File(dataFolder, "data.yml")
    }

    val userData by lazy {
        if (!dataYaml.exists())
            dataYaml.writeText("")

        YamlConfiguration.loadConfiguration(dataYaml)
    }

    override fun softEnable() {
        super.softEnable()

        this.dataFolder.mkdirs()

        registerEvents(PlayerListener(this))
        registerEvents(TagListener(this))
        registerServerEvent(eventoTorreDaMorte)
        registerCommand(TorreCommand)
        // registerCommand(TorreMinigameCommand)
        registerCommand(DreamTorreDaMorteStartCommand)

        torreDaMorte.lastWinner = userData.getString("lastWinner")?.let { UUID.fromString(it) }
    }

    override fun softDisable() {
        super.softDisable()

        if (torreDaMorte.isStarted) {
            if (torreDaMorte.isPreStart) {
                torreDaMorte.playersInQueue.toList().forEach { torreDaMorte.removeFromQueue(it) }
            } else {
                torreDaMorte.players.toList().forEach { torreDaMorte.removeFromGame(it, skipFinishCheck = false) }
            }
        }

        userData.set("lastWinner", torreDaMorte.lastWinner?.toString())
        userData.save(dataYaml)
    }
}