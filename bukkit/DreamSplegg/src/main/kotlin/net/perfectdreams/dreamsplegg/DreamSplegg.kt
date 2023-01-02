package net.perfectdreams.dreamsplegg

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamsplegg.commands.DreamSpleggStartCommand
import net.perfectdreams.dreamsplegg.commands.SpleggCommand
import net.perfectdreams.dreamsplegg.event.EventoSplegg
import net.perfectdreams.dreamsplegg.listeners.PlayerListener
import net.perfectdreams.dreamsplegg.listeners.TagListener
import net.perfectdreams.dreamsplegg.utils.Splegg
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class DreamSplegg : KotlinPlugin() {
    companion object {
        const val PREFIX = "§8[§5§lSplegg§8]§e"
    }

    val splegg = Splegg(this)
    val eventoSplegg = EventoSplegg(this)

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
        registerServerEvent(eventoSplegg)
        registerCommand(SpleggCommand)
        // registerCommand(TorreMinigameCommand)
        registerCommand(DreamSpleggStartCommand)

        splegg.lastWinner = userData.getString("lastWinner")?.let { UUID.fromString(it) }
    }

    override fun softDisable() {
        super.softDisable()

        if (splegg.isStarted) {
            if (splegg.isPreStart) {
                splegg.playersInQueue.toList().forEach { splegg.removeFromQueue(it) }
            } else {
                splegg.players.toList().forEach { splegg.removeFromGame(it, skipFinishCheck = false) }
            }
        }

        userData.set("lastWinner", splegg.lastWinner?.toString())
        userData.save(dataYaml)
    }
}