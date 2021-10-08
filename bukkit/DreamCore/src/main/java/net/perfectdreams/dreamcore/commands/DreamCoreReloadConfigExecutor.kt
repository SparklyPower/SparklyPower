package net.perfectdreams.dreamcore.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention.sendMessage
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.commands.TellExecutor.Companion.Options.player
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.stripColorCode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import java.io.File

class DreamCoreReloadConfigExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DreamCoreReloadConfigExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        plugin.loadConfig()

        context.sender.sendMessage("§aConfiguração recarregada!")
    }
}