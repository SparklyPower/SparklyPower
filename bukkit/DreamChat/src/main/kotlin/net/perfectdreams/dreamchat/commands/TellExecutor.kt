package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamvanish.DreamVanishAPI

class TellExecutor(val m: DreamChat) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(TellExecutor::class) {
        object Options : CommandOptions() {
            val receiver = player("receiver")
                .register()

            val message = greedyString("message")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val sender = context.requirePlayer()
        val receiver = args.getAndValidate(options.receiver)
        val message = args[options.message]

        if (DreamVanishAPI.isQueroTrabalhar(receiver)) {
            receiver.sendMessage("§c${sender.displayName}§c tentou te enviar §e${message}§c!")
            context.fail(CommandArguments.PLAYER_NOT_FOUND.invoke())
        }

        if (sender == receiver)
            context.fail("§cVocê não pode enviar uma mensagem para você mesmo, bobinh${sender.artigo}!")

        if (message == null) {
            // If the message is null, then the user wants to lock a tell with someone!
            m.lockedTells[sender] = receiver.name
            sender.sendMessage("§aSeu chat foi travado com ${receiver.artigo} §b${receiver.displayName}§a! Agora você pode enviar mensagens no chat e elas irão ir para a caixa privada d${receiver.artigo} §b${receiver.displayName}§a!")
            sender.sendMessage("§7Para desativar, use §6/tell lock")
            return
        }

        ChatUtils.sendTell(sender, receiver, message)
    }
}
