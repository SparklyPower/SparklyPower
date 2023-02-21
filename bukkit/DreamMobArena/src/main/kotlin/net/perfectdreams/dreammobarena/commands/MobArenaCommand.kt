package net.perfectdreams.dreammobarena.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreammobarena.DreamMobArena

class MobArenaCommand(val m: DreamMobArena) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("mobarena")) {
        subcommand(listOf("join")) {
            executor = MobArenaJoinExecutor()
        }

        subcommand(listOf("start")) {
            executor = MobArenaStartExecutor()
        }
    }

    inner class MobArenaJoinExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.mobArena.players.add(context.requirePlayer())
            context.sendMessage("Você entrou na Mob Arena!")
        }
    }

    inner class MobArenaStartExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.mobArena.start()
            context.sendMessage("Você iniciou a Mob Arena!")
        }
    }
}