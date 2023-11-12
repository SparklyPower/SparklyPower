package net.perfectdreams.dreamresourcepack.listeners

import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreambedrockintegrations.utils.isBedrockClient
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamresourcepack.DreamResourcePack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent

class MoveListener(val m: DreamResourcePack) : Listener {
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (!e.displaced)
            return

        // RP já enviada
        if (m.sentToPlayer.contains(e.player))
            return

        // Apenas mandar a resource pack na primeira "andada" que o player fizer
        m.sentToPlayer.add(e.player)

        e.player.setResourcePack(
            m.config.getString("link")!!,
            m.config.getString("hash")!!
        )
    }

    @EventHandler
    fun onResourcePackStatus(e: PlayerResourcePackStatusEvent) {
        // No need to tell the user that they rejected the resource pack if they are using Geyser
        if (!e.player.isBedrockClient) {
            when (e.status) {
                PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED -> {
                    e.player.sendMessage("§aA resource pack foi baixada e ativada com sucesso, obrigado por aceitar a nossa resource pack! ^-^")
                }

                PlayerResourcePackStatusEvent.Status.DECLINED -> {
                    e.player.sendMessage("§cAo recusar a nossa resource pack você poderá enfrentar problemas e algumas coisas podem aparecer meio... \"estranhas\".")
                    e.player.sendMessage("§cCaso você mude de ideia e queria reativar a resource pack, edite o servidor na lista de servidores para aceitar a resource pack!")
                }

                PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD -> {
                    e.player.sendMessage("§cParece que ocorreu algum problema ao baixar a resource pack, tente novamente mais tarde e desculpe pela inconveniência...")
                }

                PlayerResourcePackStatusEvent.Status.ACCEPTED -> {
                    e.player.sendMessage("§aObrigado por aceitar a resource pack! Ela está sendo baixada e, ao terminar, ela será ativada automaticamente!")
                }
            }
        }
    }
}