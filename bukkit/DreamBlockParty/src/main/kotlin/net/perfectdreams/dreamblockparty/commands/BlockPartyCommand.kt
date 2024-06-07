package net.perfectdreams.dreamblockparty.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamblockparty.DreamBlockParty
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class BlockPartyCommand(val m: DreamBlockParty) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("blockparty")) {
        permission = "dreamblockparty.joinevent"

        executor = JoinBlockPartyExecutor(m)
    }

    class JoinBlockPartyExecutor(val m: DreamBlockParty) : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()
            if (m.blockParty.isStarted && !m.blockParty.isPreStart) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(DreamBlockParty.prefix())
                    appendSpace()
                    append("O Block Party já começou!")
                }
                return
            }

            if (!m.blockParty.isStarted && !m.blockParty.isPreStart) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(DreamBlockParty.prefix())
                    appendSpace()
                    append("O Block Party não está acontecendo no momento!")
                }
                return
            }

            if (player in m.blockParty.playersInQueue) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(DreamBlockParty.prefix())
                    appendSpace()
                    append("Você já está a fila do Block Party!")
                }
                return
            }

            if (m.blockParty.isServerEvent) {
                context.sendMessage {
                    color(NamedTextColor.YELLOW)
                    append(DreamBlockParty.prefix())
                    appendSpace()
                    append("Você entrou no evento Block Party!")
                }
                m.blockParty.joinQueue(player)
            } else {
                // player.sendMessage("${DreamTorreDaMorte.PREFIX} §cO Evento Torre da Morte não está ocorrendo no momento! Se você quiser entrar na torre apenas para se divertir sem ganhar nenhuma recompensa, entre na §6/torre minigame")
                context.sendMessage {
                    color(NamedTextColor.RED)
                    append(DreamBlockParty.prefix())
                    appendSpace()
                    append("O Evento Block Party não está acontecendo no momento!")
                }
                return
            }
        }
    }
}