package net.perfectdreams.dreammochilas.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreammochilas.utils.MochilaUtils

class GetMochilaIdExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(GetMochilaIdExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val itemInMainHand = player.inventory.itemInMainHand

        if (!MochilaUtils.isMochila(itemInMainHand))
            context.fail("§cVocê não está segurando uma mochila!")

        val mochilaId = MochilaUtils.getMochilaId(itemInMainHand) ?: run {
            context.fail("§cVocê está segurando uma mochila que não possui um ID!")
        }

        context.sendMessage("§aID da mochila: §e$mochilaId")
    }
}