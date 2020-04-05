package net.perfectdreams.dreamminarecheada

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamminarecheada.commands.MinaRecheadaCommand
import net.perfectdreams.dreamminarecheada.events.MinaRecheada
import net.perfectdreams.dreamminarecheada.listeners.InteractListener
import java.io.File

class DreamMinaRecheada : KotlinPlugin() {
    companion object {
        lateinit var INSTANCE: DreamMinaRecheada
        const val PREFIX = "§8[§e§lMina §6§lR§e§le§6§lc§e§lh§6§le§e§la§6§ld§e§la§8] "
    }

    val minaRecheada = MinaRecheada()

    override fun softEnable() {
        INSTANCE = this

        DreamCore.INSTANCE.dreamEventManager.events.add(minaRecheada)

        this.dataFolder.mkdirs()
        if (File(this.dataFolder, "minarecheadadata.json").exists()) {
            minaRecheada.minaRecheadaData = DreamUtils.gson.fromJson(File(this.dataFolder, "minarecheadadata.json").readText())
        }

        registerEvents(InteractListener(this))
        registerCommand(MinaRecheadaCommand(this))
    }
}
