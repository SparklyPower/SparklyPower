package net.perfectdreams.dreamtorredamorte

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamtorredamorte.commands.TorreCommand
import net.perfectdreams.dreamtorredamorte.listeners.PlayerListener
import net.perfectdreams.dreamtorredamorte.utils.TorreDaMorte

class DreamTorreDaMorte : KotlinPlugin() {
    companion object {
        const val PREFIX = "§8[§c§lTorre da §4§lMorte§8]§e"
    }

    val torreDaMorte = TorreDaMorte(this)

    override fun softEnable() {
        super.softEnable()

        this.dataFolder.mkdirs()

        registerEvents(PlayerListener(this))
        registerCommand(TorreCommand(this))
    }

    override fun softDisable() {
        super.softDisable()
    }
}