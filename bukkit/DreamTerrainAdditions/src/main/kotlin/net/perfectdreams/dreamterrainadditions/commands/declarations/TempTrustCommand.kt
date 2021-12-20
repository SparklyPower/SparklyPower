package net.perfectdreams.dreamterrainadditions.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamterrainadditions.commands.TempTrustExecutor
import net.perfectdreams.dreamterrainadditions.commands.TempTrustListExecutor

object TempTrustCommand: SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("temptrust")) {
        executor = TempTrustExecutor
        subcommand(listOf("list")) {
            executor = TempTrustListExecutor
        }
    }
}