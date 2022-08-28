package net.perfectdreams.dreamterrainadditions.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import net.perfectdreams.dreamterrainadditions.commands.TempTrustExecutor
import net.perfectdreams.dreamterrainadditions.commands.TempTrustListExecutor

class TempTrustCommand(val plugin: DreamTerrainAdditions): SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("temptrust", "trusttemp")) {
        executor = TempTrustExecutor(plugin)

        subcommand(listOf("list")) {
            executor = TempTrustListExecutor(plugin)
        }
    }
}