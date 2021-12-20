package net.perfectdreams.dreamterrainadditions.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamterrainadditions.commands.TempTrustListExecutor

object TempTrustListCommand: SparklyCommandDeclarationWrapper {
    override fun declaration(): SparklyCommandDeclaration = sparklyCommand(listOf("temptrustlist", "listtemptrust")) {
        executor = TempTrustListExecutor
    }
}