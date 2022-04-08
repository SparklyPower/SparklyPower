package net.perfectdreams.dreamxizum.lobby

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.citizensnpcs.api.event.NPCClickEvent
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerSetOf
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamvanish.DreamVanishAPI
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.config.XizumConfig.xizumWorld
import net.perfectdreams.dreamxizum.dao.Combatant
import net.perfectdreams.dreamxizum.dao.Duelist
import net.perfectdreams.dreamxizum.extensions.available
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.extensions.freeFromBattle
import net.perfectdreams.dreamxizum.extensions.prepareToBattle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.InitialPage
import net.perfectdreams.dreamxizum.tasks.RankedQueueUser
import net.perfectdreams.dreamxizum.tasks.UpdateLeaderboardTask
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerTeleportEvent

class Lobby : Listener {
    companion object {
        val allowedCommands = setOf("spawn", "tell", "msg", "r", "reply", "quickreply", ".", "a", "adminchat", "pay",
            "ban", "kick", "warn", "tp", "cash", "dreamcash", "pesadelos", "money", "x1", "xizum", "dreamxizum").mapTo(mutableSetOf()) { "/$it" }

        val creatingBattle = mutablePlayerSetOf { it.teleportToServerSpawn() }
    }

    val menu = XizumConfig.models.locations.menu.toBukkitLocation()

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        with (event) {
            if (player !in creatingBattle) return
            if (to.world != menu.world) return
            if (to.distance(menu) > 5) cancelBattleCreation(player)
        }
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) { with (event.player) { if (this in creatingBattle) cancelBattleCreation(this) } }

    fun cancelBattleCreation(player: Player) = with(player) {
        sendMessage("${DreamXizum.PREFIX} A criação do xizum foi cancelada.")
        battle?.let { Matchmaker.cancelBattle(it) }
        creatingBattle.remove(this)
        freeFromBattle()
    }

    @EventHandler
    fun onRightClick(event: NPCRightClickEvent) = onClick(event)
    @EventHandler
    fun onLeftClick(event: NPCLeftClickEvent) = onClick(event)

    fun onClick(event: NPCClickEvent) {
        DreamXizum.INSTANCE.schedule {
            val id = event.npc.id
            if (id != CitizensNPCs.npcIds.normalNPCId && id != CitizensNPCs.npcIds.rankedNPCId) return@schedule

            val player = event.clicker

            if (player.gameMode != GameMode.SURVIVAL) return@schedule player.sendMessage("${DreamXizum.PREFIX} Entre no modo sobrevivência para batalhar no xizum.")
            if (DreamVanishAPI.isVanished(player)) return@schedule player.sendMessage("${DreamXizum.PREFIX} Lutar invisível é moleza, hein? Use ${highlight("/vanish")} para poder batalhar no xizum.")
            if (DreamVanishAPI.isQueroTrabalhar(player)) return@schedule player.sendMessage("${DreamXizum.PREFIX} Desative o ${highlight("/querotrabalhar")}. Como os outros irão aceitar seu convite se você está \"offline\"?")

            if (id == CitizensNPCs.npcIds.rankedNPCId) {
                switchContext(SynchronizationContext.ASYNC)

                val combatant = Combatant.fetch(player.uniqueId)

                switchContext(SynchronizationContext.SYNC)

                if (!UpdateLeaderboardTask.hasAnnounced) return@schedule player.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND,
                    if (!Matchmaker.hasSeasonStarted) "§cA temporada ${Holograms.timeMessage.lowercase()}." else "§cA temporada se inciará em breve.")

                if (combatant.banned) return@schedule player.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND,
                    "§cVocê foi banido de participar das competições do Xizum por comportamento antidesportivo.")

                if (!combatant.tos) return@schedule player.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND,
                    "${DreamXizum.PREFIX} Nós zelamos por uma experiência harmoniosa e amigável no servidor. Caso se envolva em quaisquer escandâlos de teor tóxico em virtude do Xizum, " +
                            "seu privilégio de jogar no modo competitivo será revogado e você não receberá as recompensas conquistadas. Se você estiver de acordo, use o comando \"${highlight("/aceitar termos")}\".")

                switchContext(SynchronizationContext.ASYNC)

                val duelist = Duelist.fetch(player.uniqueId)

                switchContext(SynchronizationContext.SYNC)

                player.available = !player.available

                if (!player.available) {
                    Matchmaker.rankedQueue.add(RankedQueueUser(duelist))
                    player.blockCommandsExcept(allowedCommands, "Você não pode usar esse comando na fila de partidas competitivas.")
                    player.sendMessage("${DreamXizum.PREFIX} Você está esperando por uma partida competitiva. Caso queira sair, clique novamente no NPC.")
                } else Matchmaker.removeFromQueue(player)
            } else {
                if (!player.available) return@schedule player.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND,
                    "§cSaia da fila competitiva para jogar uma partida casual.")

                player.teleport(menu)
                player.prepareToBattle()
                player.blockCommandsExcept(allowedCommands, "Você não pode usar esse comando durante a criação do xizum.")

                creatingBattle.add(player)
                Paginator.fetch(player).addAndShowPage(InitialPage())
                player.sendMessage("${DreamXizum.PREFIX} Digite ${highlight("/spawn")} para cancelar a criação do xizum.")
            }
        }
    }

    @EventHandler
    fun onGrowth(event: BlockGrowEvent) { with(event) { isCancelled = block.world == xizumWorld } }
    @EventHandler
    fun onSpread(event: BlockSpreadEvent) { with(event) { isCancelled = block.world == xizumWorld } }
}