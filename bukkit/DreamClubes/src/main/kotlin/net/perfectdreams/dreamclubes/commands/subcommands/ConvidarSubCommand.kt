package net.perfectdreams.dreamclubes.commands.subcommands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.toBaseComponent
import net.perfectdreams.dreamcore.utils.toTextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ConvidarSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        async {
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@async
            }

            val onlinePlayer = Bukkit.getPlayerExact(args[0])

            if (onlinePlayer == null) {
                // Player inexistente!
                player.sendMessage("${DreamClubes.PREFIX} §cPlayer inexistente!")
                return@async
            }

            val invitedMemberClube = ClubeAPI.getPlayerClube(onlinePlayer)

            if (invitedMemberClube != null) {
                player.sendMessage("${DreamClubes.PREFIX} §b${onlinePlayer.displayName}§c já está em um clube!")
                return@async
            }

            toSync()

            if (m.pendingInvites.containsKey(onlinePlayer.uniqueId)) {
                player.sendMessage("${DreamClubes.PREFIX} §b${onlinePlayer.displayName}§c já tem um convite pendente! Tente novamente mais tarde...")
                return@async
            }

            m.pendingInvites[onlinePlayer.uniqueId] = clube.id.value
            onlinePlayer.sendMessage("§dVocê recebeu um convite para entrar na ${clube.shortName}".centralize())
            onlinePlayer.sendMessage(
                "§a§l[§aCLIQUE AQUI PARA ACEITAR§a§l]"
                    .toTextComponent()
                    .apply {
                        hoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            "§aClique para aceitar o convite!".toBaseComponent()
                        )
                        clickEvent = ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/clube aceitar"
                        )
                    }
            )

            onlinePlayer.sendMessage("§7Após 30 segundos o pedido irá expirar automaticamente.".centralize())

            player.sendMessage("${DreamClubes.PREFIX} §aPedido enviado com sucesso! Se não aceitar em 30 segundos, o convite irá expirar!")

            waitFor(600)
            val isSameInvite = m.pendingInvites[onlinePlayer.uniqueId] == clube.id.value
            if (isSameInvite) {
                m.pendingInvites.remove(onlinePlayer.uniqueId)
                onlinePlayer.sendMessage("§cO convite expirou, very sad...")
                player.sendMessage("§cParece que §b${onlinePlayer.displayName}§c não quis aceitar o seu convite... Da próxima vez, que tal fazer um convite com lantejoulas bem bonitin?")
            }
        }
    }
}