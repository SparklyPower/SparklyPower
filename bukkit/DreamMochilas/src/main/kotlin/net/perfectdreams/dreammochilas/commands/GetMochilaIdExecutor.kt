package net.perfectdreams.dreammochilas.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.persistence.PersistentDataType

class GetMochilaIdExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(GetMochilaIdExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val itemInMainHand = player.inventory.itemInMainHand

        if (!MochilaUtils.isMochila(itemInMainHand)) {
            context.sendMessage("§cVocê não está segurando uma mochila!")
            val forcefulId = itemInMainHand.itemMeta.persistentDataContainer.get(MochilaUtils.MOCHILA_ID_KEY, PersistentDataType.LONG)
            if (forcefulId != null) {
                context.sendMessage("§eMas o item parece ter um ID de mochila! §e$forcefulId")
            }
            return
        }

        val mochilaId = MochilaUtils.getMochilaId(itemInMainHand) ?: run {
            context.fail("§cVocê está segurando uma mochila que não possui um ID!")
        }

        context.sendMessage("§aID da mochila: §e$mochilaId")
    }
}