package net.perfectdreams.pantufa.interactions.commands.declarations

import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserDiscordUserExecutor
import net.perfectdreams.pantufa.interactions.commands.MinecraftUserPlayerNameExecutor

class MinecraftUserCommand(val m: PantufaBot)  : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand(
        name = "mcuser",
        description = "Veja a conta associada ao SparklyPower de um usuário"
    ) {
        subcommand("player", "Veja a conta associada ao SparklyPower pelo nome de um player no SparklyPower") {
            executor = MinecraftUserPlayerNameExecutor(m)
        }

        subcommand("user", "Veja a conta associada ao SparklyPower pela conta no Discord") {
            executor = MinecraftUserDiscordUserExecutor(m)
        }
    }
}