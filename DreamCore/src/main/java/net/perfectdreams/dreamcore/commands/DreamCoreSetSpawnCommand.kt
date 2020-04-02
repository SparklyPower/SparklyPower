package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase

object DreamCoreSetSpawnCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("dreamcore set_spawn")) {
        permission = "dreamcore.setup"

        executes {
            plugin.spawn = player.location
            plugin.userData.set("spawnLocation", player.location)
            plugin.saveConfig()

            sender.sendMessage("§aSpawn atualizado!")
        }
    }
}