package net.perfectdreams.dreamcore.commands.declarations

import net.perfectdreams.dreamcore.commands.*
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

object DreamCoreCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamcore")) {
        subcommand(listOf("script")) {
            subcommand(listOf("eval")) {
                executor = DreamCoreEvalExecutor
                permissions = listOf("dreamcore.setup")
            }

            subcommand(listOf("unload")) {
                executor = DreamCoreUnloadExecutor
                permissions = listOf("dreamcore.setup")
            }

            subcommand(listOf("reload")) {
                executor = DreamCoreReloadExecutor
                permissions = listOf("dreamcore.setup")
            }
        }

        subcommand(listOf("config")) {
            subcommand(listOf("reload")) {
                executor = DreamCoreReloadConfigExecutor
                permissions = listOf("dreamcore.setup")
            }

            subcommand(listOf("spawn")) {
                executor = DreamCoreSetSpawnExecutor
                permissions = listOf("dreamcore.setup")
            }
        }

        executor = DreamCoreExecutor
        permissions = listOf("dreamcore.setup")
    }
}