package net.perfectdreams.dreamxizum.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.title.Title
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.seconds
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.config.XizumConfig

class XizumExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(XizumExecutor::class)
    private val style = Style.style(TextColor.color(0x29d680))
    private val title = Component.text("Bem vindo ao Xizum").style(style)
    private val subtitle = Component.text("pronto para um desafio?").style(style)
    private val times = Title.Times.times(1.seconds, 3.seconds, 1.seconds)
    private val lobby = XizumConfig.models.locations.lobby.toBukkitLocation()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        player.teleport(lobby)
        player.showTitle(Title.title(title, subtitle, times))
    }
}