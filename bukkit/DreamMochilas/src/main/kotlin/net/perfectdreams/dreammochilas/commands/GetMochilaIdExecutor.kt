package net.perfectdreams.dreammochilas.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.commands.GetMochilaExecutor.Companion.Options.damageValue

class GetMochilaIdExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(GetMochilaIdExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val itemInMainHand = player.inventory.itemInMainHand

        itemInMainHand.getStoredMetadata("isMochila")?.toBoolean() ?: run {
            context.fail("§cVocê não está segurando uma mochila!")
        }
        val mochilaId = itemInMainHand.getStoredMetadata("mochilaId")?.toLong() ?: run {
            context.fail("§cVocê está segurando uma mochila que não possui um ID!")
        }

        context.sendMessage("§aID da mochila: §e$mochilaId")
    }
}