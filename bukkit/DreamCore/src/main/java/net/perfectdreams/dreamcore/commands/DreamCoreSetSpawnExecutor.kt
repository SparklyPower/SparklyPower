package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class DreamCoreSetSpawnExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        plugin.spawn = player.location
        plugin.userData.set("spawnLocation", player.location)
        plugin.saveConfig()

        player.sendMessage("§aSpawn atualizado!")
    }
}