package net.perfectdreams.dreamchunkloader.commands.declarations

import net.perfectdreams.dreamchunkloader.commands.GiveChunkLoaderExecutor
import net.perfectdreams.dreamchunkloader.commands.PluginTicketsChunkLoaderExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

object DreamChunkLoaderCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamchunkloader")) {
        permissions = listOf("dreamchunkloader.setup")

        subcommand(listOf("give")) {
            permissions = listOf("dreamchunkloader.setup")

            executor = GiveChunkLoaderExecutor
        }

        subcommand(listOf("chunktickets")) {
            permissions = listOf("dreamchunkloader.setup")

            executor = PluginTicketsChunkLoaderExecutor
            
        }
    }
}