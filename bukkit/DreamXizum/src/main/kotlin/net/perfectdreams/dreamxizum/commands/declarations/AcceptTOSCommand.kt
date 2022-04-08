package net.perfectdreams.dreamxizum.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamxizum.commands.AcceptTOSExecutor

object AcceptTOSCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("aceitar")) {
        subcommand(listOf("termos")) {
            executor = AcceptTOSExecutor
        }
    }
}