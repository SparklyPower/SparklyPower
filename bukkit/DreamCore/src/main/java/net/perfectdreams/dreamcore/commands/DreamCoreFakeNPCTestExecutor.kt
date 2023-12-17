package net.perfectdreams.dreamcore.commands

import net.minecraft.tags.FluidTags
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld
import org.bukkit.entity.EntityType
import kotlin.time.measureTime

class DreamCoreFakeNPCTestExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        if (true) {
            repeat(1_000) {
                Bukkit.getWorld("world")!!.spawnEntity(
                    Location(Bukkit.getWorld("world"), 1094.0, 64.0, -896.0),
                    EntityType.ARMOR_STAND,
                )
            }
            player.sendMessage("spawned!")
            return
        }
        // plugin.sparklyNPCManager.spawnFakePlayer(plugin, player.location, "hewwo")
        player.scheduler.execute(plugin, {
            println("scheduled and executed!")
            player.sendMessage("scheduled and executed!")
        }, {
            println("scheduled but has been retired!")
            player.sendMessage("scheduled but has been retired!")
        }, 100L)
    }
}