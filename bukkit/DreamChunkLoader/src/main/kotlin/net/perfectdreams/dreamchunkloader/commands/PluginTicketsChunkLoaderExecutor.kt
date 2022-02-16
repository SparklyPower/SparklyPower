package net.perfectdreams.dreamchunkloader.commands

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamchunkloader.DreamChunkLoader
import net.perfectdreams.dreamchunkloader.data.ChunkLoaderItemInfo
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType

class PluginTicketsChunkLoaderExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(PluginTicketsChunkLoaderExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()


        context.sendMessage("§aTickets em Chunks:")
        player.world.pluginChunkTickets.forEach { (t, u) ->
            player.sendMessage("${t.name} -> $u")
        }
    }
}