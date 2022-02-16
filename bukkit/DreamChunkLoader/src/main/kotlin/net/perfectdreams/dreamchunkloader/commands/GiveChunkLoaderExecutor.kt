package net.perfectdreams.dreamchunkloader.commands

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamchunkloader.DreamChunkLoader
import net.perfectdreams.dreamchunkloader.data.ChunkLoaderBlockInfo
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

class GiveChunkLoaderExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(GiveChunkLoaderExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        player.inventory.addItem(
            ItemStack(Material.PLAYER_HEAD).meta<SkullMeta> {
                persistentDataContainer.set(
                    DreamChunkLoader.CHUNK_LOADER_INFO_KEY,
                    PersistentDataType.STRING,
                    Json.encodeToString(ChunkLoaderItemInfo(0))
                )
            }
        )

        context.sendMessage("§aVocê recebeu um chunk loader!")
    }
}