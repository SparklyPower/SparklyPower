package net.perfectdreams.dreamxizum.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamxizum.commands.XizumExecutor
import net.perfectdreams.dreamxizum.commands.subcommands.*

object XizumCommand : SparklyCommandDeclarationWrapper { override fun declaration() = getDeclaration("xizum") }
object X1Command : SparklyCommandDeclarationWrapper { override fun declaration() = getDeclaration("x1") }

private fun getDeclaration(command: String) = sparklyCommand(listOf(command)) {
    executor = XizumExecutor

    subcommand(listOf("aceitar")) {
        executor = XizumAcceptExecutor
    }

    subcommand(listOf("recusar")) {
        executor = XizumRefuseExecutor
    }

    subcommand(listOf("convidar")) {
        executor = XizumInviteExecutor
    }

    subcommand(listOf("expulsar")) {
        executor = XizumRemoveExecutor
    }

    subcommand(listOf("rank")) {
        executor = XizumRankExecutor
    }
}