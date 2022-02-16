package net.perfectdreams.dreammochilas.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreammochilas.commands.*

object DreamMochilaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreammochila")) {
        permissions = listOf("sparklymochilas.setup")

        subcommand(listOf("get")) {
            permissions = listOf("sparklymochilas.setup")
            executor = GetMochilaExecutor
        }

        subcommand(listOf("player")) {
            permissions = listOf("sparklymochilas.setup")
            executor = GetPlayerMochilasExecutor
        }

        subcommand(listOf("memory_mochilas")) {
            permissions = listOf("sparklymochilas.setup")
            executor = MochilasMemoryExecutor
        }

        subcommand(listOf("hack_tests")) {
            subcommand(listOf("interact_and_open")) {
                permissions = listOf("sparklymochilas.setup")
                executor = FakeInteractAndOpenExecutor
            }

            subcommand(listOf("interact_auto_click")) {
                permissions = listOf("sparklymochilas.setup")
                executor = FakeInteractAutoClickExecutor
            }
        }
    }
}