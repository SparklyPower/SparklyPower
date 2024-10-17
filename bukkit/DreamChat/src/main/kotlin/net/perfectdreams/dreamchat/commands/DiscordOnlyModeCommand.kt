package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class DiscordOnlyModeCommand(val m: DreamChat) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("discordonlymode")) {
        permission = "dreamchat.discordonlymode"

        subcommand(listOf("enable")) {
            permission = "dreamchat.discordonlymode"
            executor = EnableDiscordOnlyModeExecutor(m)
        }

        subcommand(listOf("disable")) {
            permission = "dreamchat.discordonlymode"
            executor = DisableDiscordOnlyModeExecutor(m)
        }

        executor = ViewDiscordOnlyModeExecutor(m)
    }

    class ViewDiscordOnlyModeExecutor(val m: DreamChat) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            if (m.onlyLetConnectedDiscordAccountsTalk) {
                context.sendMessage {
                    content("Modo \"Apenas pode falar no chat se a conta está conectada no Discord\" está ativado. Desative usando \"/discordonlymode disable\"")
                }
            } else {
                context.sendMessage {
                    content("Modo \"Apenas pode falar no chat se a conta está conectada no Discord\" está desativado. Ative usando \"/discordonlymode enable\"")
                }
            }
        }
    }

    class EnableDiscordOnlyModeExecutor(val m: DreamChat) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.onlyLetConnectedDiscordAccountsTalk = true
            context.sendMessage {
                content("Modo \"Apenas pode falar no chat se a conta está conectada no Discord\" ativado!")
            }
        }
    }

    class DisableDiscordOnlyModeExecutor(val m: DreamChat) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            m.onlyLetConnectedDiscordAccountsTalk = false
            context.sendMessage {
                content("Modo \"Apenas pode falar no chat se a conta está conectada no Discord\" desativado!")
            }
        }
    }
}