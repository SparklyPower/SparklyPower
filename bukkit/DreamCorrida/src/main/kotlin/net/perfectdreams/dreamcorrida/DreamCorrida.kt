package net.perfectdreams.dreamcorrida

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcorrida.commands.CorridaCommand
import net.perfectdreams.dreamcorrida.events.EventoCorrida
import net.perfectdreams.dreamcorrida.listeners.PlayerListener
import net.perfectdreams.dreamcorrida.listeners.TagListener
import net.perfectdreams.dreamcorrida.utils.Corrida
import net.perfectdreams.dreamcorrida.utils.LocationWrapper
import java.io.File
import java.util.*


class DreamCorrida : KotlinPlugin() {
    companion object {
        const val PREFIX = "§8[§3§lCorrida§8]§e"
    }

    lateinit var config: Config
    lateinit var eventoCorrida: EventoCorrida
    val availableCorridas = mutableListOf<Corrida>()
    var lastWinner: UUID? = null

    override fun softEnable() {
        super.softEnable()

        this.dataFolder.mkdirs()

        val corridaFolders = File(this.dataFolder, "corridas")
        corridaFolders.mkdirs()
        corridaFolders.listFiles().forEach {
            if (it.extension == "json") {
                availableCorridas.add(DreamUtils.gson.fromJson(it.readText()))
            }
        }

        val file = File(this.dataFolder, "config.json")
        if (!file.exists()) {
            file.writeText(
                DreamUtils.gson.toJson(
                    Config(UUID.randomUUID())
                )
            )
        }

        config = DreamUtils.gson.fromJson(file.readText(), Config::class.java)
        eventoCorrida = EventoCorrida(this)

        registerCommand(CorridaCommand(this))
        registerEvents(PlayerListener(this))
        registerEvents(TagListener(this))

        DreamCore.INSTANCE.dreamEventManager.events.add(eventoCorrida)
    }

    override fun softDisable() {
        super.softDisable()

        DreamCore.INSTANCE.dreamEventManager.events.remove(eventoCorrida)
    }

    class Config(var winner: UUID)
}