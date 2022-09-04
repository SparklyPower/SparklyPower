package net.perfectdreams.dreamclubes.commands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread

class ClubesExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        m.launchAsyncThread {
            val clube = ClubeAPI.getPlayerClube(player)
            val selfMember = clube?.retrieveMember(player)

            onMainThread {
                player.sendMessage("§8[ §bClube §8]".centralizeHeader())
                var isOwner = false
                var isAdmin = false
                var hasClan = false
                if (clube != null) {
                    hasClan = true
                    if (selfMember?.canExecute(ClubePermissionLevel.ADMIN) == true) {
                        isAdmin = true
                    }
                    if (selfMember?.canExecute(ClubePermissionLevel.OWNER) == true) {
                        isOwner = true
                    }
                }
                if (!hasClan) {
                    player.sendMessage("/clube criar - Cria uma clube! §aCusto: 150000 sonecas")
                }
                player.sendMessage("/clube lista - Mostra os 10 clube mais poderosos")
                player.sendMessage("/clube leaderboard - Mostra os 10 players com maior KDR")
                player.sendMessage("/clube kdr - Mostra o seu KDR")
                player.sendMessage("/clube resetkdr - Reseta o seu KDR")
                if (hasClan && !isOwner) {
                    player.sendMessage("/clube sair - Sai do seu clube atual")
                }
                if (hasClan && isOwner) {
                    player.sendMessage("/clube deletar - Exclui o seu clube")
                    player.sendMessage("/clube admin - Deixa um player como admin")
                    player.sendMessage("/clube dono - Transfere a posse do clube para outro player")
                }
                if (hasClan) {
                    player.sendMessage("/clube coords - Mostra as coordenadas dos seus amigos")
                    player.sendMessage("/clube vitals - Mostra o status de seus amigos")
                    player.sendMessage("/clube membros - Mostra os membros do clube")
                }
                if (hasClan && isAdmin) {
                    player.sendMessage("/clube tag - Altera a tag do clube")
                    player.sendMessage("/clube nome - Altera o nome do clube")
                    player.sendMessage("/clube kick - Remove algu\u00e9m do clube")
                    player.sendMessage("/clube prefixo - Coloca um prefixo no player")
                    player.sendMessage("/clube setcasa - Marca a casa do clube")
                    player.sendMessage("/clube delcasa - Deleta a casa do clube")
                }
                if (hasClan) {
                    player.sendMessage("/clube casa - Teletransporta para a casa do clube")
                }
                if (hasClan) {
                    player.sendMessage("/. - Chat do Clube")
                }
                player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
            }
        }
    }
}