package net.perfectdreams.dreamenderhopper.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamenderhopper.DreamEnderHopper
import net.perfectdreams.dreamenderhopper.commands.GiveEnderHopperExecutor

class DreamEnderHopperDeclaration(private val m: DreamEnderHopper) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamenderhopper")) {
        subcommand(listOf("give")) {
            permissions = listOf("dreamenderhopper.give")
            executor = GiveEnderHopperExecutor(m)
        }
    }
}