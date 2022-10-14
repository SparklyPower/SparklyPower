package net.perfectdreams.dreamclubes.commands.declarations

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.KDRExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class KDRCommand(val m: DreamClubes) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("kdr")) {
        executor = KDRExecutor(m)
    }
}