package net.perfectdreams.dreamclubes.commands.subcommands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.toBaseComponent
import net.perfectdreams.dreamcore.utils.toTextComponent
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.Bukkit
import kotlin.collections.set

class ConvidarClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val playerName = word("player_name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val playerName = args[options.playerName]

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@withPlayerClube
            }

            val onlinePlayer = Bukkit.getPlayerExact(playerName)

            if (onlinePlayer == null || DreamVanishAPI.isQueroTrabalhar(onlinePlayer)) {
                // Player inexistente!
                player.sendMessage("${DreamClubes.PREFIX} §cPlayer inexistente!")
                return@withPlayerClube
            }

            val invitedMemberClube = onAsyncThread { ClubeAPI.getPlayerClube(onlinePlayer) }

            if (invitedMemberClube != null) {
                player.sendMessage("${DreamClubes.PREFIX} §b${onlinePlayer.displayName}§c já está em um clube!")
                return@withPlayerClube
            }

            if (m.pendingInvites.containsKey(onlinePlayer.uniqueId)) {
                player.sendMessage("${DreamClubes.PREFIX} §b${onlinePlayer.displayName}§c já tem um convite pendente! Tente novamente mais tarde...")
                return@withPlayerClube
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

            delayTicks(600L)

            val isSameInvite = m.pendingInvites[onlinePlayer.uniqueId] == clube.id.value
            if (isSameInvite) {
                m.pendingInvites.remove(onlinePlayer.uniqueId)
                onlinePlayer.sendMessage("§cO convite expirou, very sad...")
                player.sendMessage("§cParece que §b${onlinePlayer.displayName}§c não quis aceitar o seu convite... Da próxima vez, que tal fazer um convite com lantejoulas bem bonitin?")
            }
        }
    }
}