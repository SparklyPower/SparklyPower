package net.perfectdreams.dreamloja.commands

import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.tables.Shops

class LojaManageExecutor(m: DreamLoja) : LojaExecutorBase(m) {
    companion object : SparklyCommandExecutorDeclaration(LojaManageExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        player.sendMessage("§8[ §9Minha Loja §8]".centralizeHeader())
        context.sendMessage {
            appendCommand("/loja manage set")
            append(" - Altera a posição da sua loja")
        }
        context.sendMessage {
            appendCommand("/loja manage icon")
            append(" - Altera o ícone da sua loja")
        }
        context.sendMessage {
            appendCommand("/loja manage delete")
            append(" - Deleta a sua loja")
        }
        context.sendMessage {
            appendCommand("/loja manage list")
            append(" - Mostra todas as suas lojas")
        }

        player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
    }
}