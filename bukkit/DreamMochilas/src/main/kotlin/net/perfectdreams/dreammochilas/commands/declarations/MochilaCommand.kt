package net.perfectdreams.dreammochilas.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreammochilas.commands.*

object MochilaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("mochila")) {
        permissions = listOf("sparklymochilas.give")

        subcommand(listOf("get")) {
            executor = GetMochilaExecutor
        }

        subcommand(listOf("player")) {
            executor = GetPlayerMochilasExecutor
        }

        subcommand(listOf("memory_mochilas")) {
            executor = MochilasMemoryExecutor
        }

        subcommand(listOf("hack_tests")) {
            subcommand(listOf("interact_and_open")) {
                executor = FakeInteractAndOpenExecutor
            }

            subcommand(listOf("interact_auto_click")) {
                executor = FakeInteractAutoClickExecutor
            }
        }
    }
}