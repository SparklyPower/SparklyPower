package net.perfectdreams.dreamtntrun

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamtntrun.commands.DreamTNTRunStartCommand
import net.perfectdreams.dreamtntrun.commands.TNTRunCommand
import net.perfectdreams.dreamtntrun.event.EventoTNTRun
import net.perfectdreams.dreamtntrun.listeners.PlayerListener
import net.perfectdreams.dreamtntrun.listeners.TagListener
import net.perfectdreams.dreamtntrun.utils.TNTRun
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class DreamTNTRun : KotlinPlugin() {
    companion object {
        const val PREFIX = "§8[§c§lTNT Run§8]§e"
    }

    val TNTRun = TNTRun(this)
    val eventoTNTRun = EventoTNTRun(this)

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
        registerServerEvent(eventoTNTRun)
        registerCommand(TNTRunCommand)
        // registerCommand(TorreMinigameCommand)
        registerCommand(DreamTNTRunStartCommand)

        TNTRun.lastWinner = userData.getString("lastWinner")?.let { UUID.fromString(it) }
    }

    override fun softDisable() {
        super.softDisable()

        if (TNTRun.isStarted) {
            if (TNTRun.isPreStart) {
                TNTRun.playersInQueue.toList().forEach { TNTRun.removeFromQueue(it) }
            } else {
                TNTRun.players.toList().forEach { TNTRun.removeFromGame(it, skipFinishCheck = false) }
            }
        }

        userData.set("lastWinner", TNTRun.lastWinner?.toString())
        userData.save(dataYaml)
    }
}