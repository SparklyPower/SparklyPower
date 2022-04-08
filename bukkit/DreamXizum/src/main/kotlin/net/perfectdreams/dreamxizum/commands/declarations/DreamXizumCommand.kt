package net.perfectdreams.dreamxizum.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamxizum.commands.DreamXizumBanExecutor
import net.perfectdreams.dreamxizum.commands.DreamXizumCancelExecutor
import net.perfectdreams.dreamxizum.commands.DreamXizumReloadExecutor

object DreamXizumCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamxizum")) {
        subcommand(listOf("reload")) {
            executor = DreamXizumReloadExecutor
            permissions = listOf("dreamxizum.setup")
        }

        subcommand(listOf("cancelar")) {
            executor = DreamXizumCancelExecutor
            permissions = listOf("dreamxizum.moderate")
        }

        subcommand(listOf("banir")) {
            executor = DreamXizumBanExecutor
            permissions = listOf("dreamxizum.moderate")
        }
    }
}