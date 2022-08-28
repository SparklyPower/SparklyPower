package net.perfectdreams.dreamcore.commands.declarations

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.commands.*
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class DreamCoreCommand(val plugin: DreamCore) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamcore")) {
        subcommand(listOf("script")) {
            subcommand(listOf("eval")) {
                executor = DreamCoreEvalExecutor(plugin)
                permissions = listOf("dreamcore.setup")
            }

            subcommand(listOf("unload")) {
                executor = DreamCoreUnloadExecutor(plugin)
                permissions = listOf("dreamcore.setup")
            }

            subcommand(listOf("reload")) {
                executor = DreamCoreReloadExecutor(plugin)
                permissions = listOf("dreamcore.setup")
            }
        }

        subcommand(listOf("config")) {
            subcommand(listOf("reload")) {
                executor = DreamCoreReloadConfigExecutor(plugin)
                permissions = listOf("dreamcore.setup")
            }

            subcommand(listOf("spawn")) {
                executor = DreamCoreSetSpawnExecutor(plugin)
                permissions = listOf("dreamcore.setup")
            }
        }

        executor = DreamCoreExecutor()
        permissions = listOf("dreamcore.setup")
    }
}