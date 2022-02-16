package net.perfectdreams.dreamchunkloader

import net.perfectdreams.dreamchunkloader.commands.GiveChunkLoaderExecutor
import net.perfectdreams.dreamchunkloader.commands.PluginTicketsChunkLoaderExecutor
import net.perfectdreams.dreamchunkloader.commands.declarations.DreamChunkLoaderCommand
import net.perfectdreams.dreamchunkloader.listeners.BlockListener
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.registerEvents

class DreamChunkLoader : KotlinPlugin() {
	companion object {
		val CHUNK_LOADER_INFO_KEY = SparklyNamespacedKey("chunk_loader_info")
	}

	override fun softEnable() {
		super.softEnable()

		registerCommand(
			DreamChunkLoaderCommand,
			GiveChunkLoaderExecutor(),
			PluginTicketsChunkLoaderExecutor()
		)

		registerEvents(BlockListener(this))
	}

	override fun softDisable() {
		super.softDisable()
	}
}