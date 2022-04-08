package net.perfectdreams.dreamxizum.gui

import com.gmail.nossr50.api.PartyAPI
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.canPay
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerSetOf
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.BattleItems
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.dao.Kit
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.buttons.Button
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.FinalPage
import net.perfectdreams.dreamxizum.gui.pages.InitialPage
import net.perfectdreams.dreamxizum.gui.pages.betting.BettingPage
import net.perfectdreams.dreamxizum.gui.pages.items.enchantments.EnchantBowPage
import net.perfectdreams.dreamxizum.gui.pages.items.enchantments.EnchantCrossbowPage
import net.perfectdreams.dreamxizum.gui.pages.items.enchantments.EnchantShieldPage
import net.perfectdreams.dreamxizum.gui.pages.items.enchantments.EnchantTridentPage
import net.perfectdreams.dreamxizum.gui.pages.kits.PlayerKitsPage
import net.perfectdreams.dreamxizum.lobby.Lobby
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

class Paginator {
    companion object {
        private val partying = mutablePlayerSetOf()
        private val instances = mutablePlayerMapOf<Paginator>()
        fun fetch(player: Player) = instances[player] ?: Paginator().apply {
            instances[player] = this
            user = player
        }

        fun inferNextPage(player: Player, limit: Int) =
             with (player.battle!!.options.items) {
                when {
                    limit > 3 && any { it.type == Material.BOW } -> EnchantBowPage(player)
                    limit > 2 && any { it.type == Material.CROSSBOW } -> EnchantCrossbowPage(player)
                    limit > 1 && any { it.type == Material.TRIDENT } -> EnchantTridentPage(player)
                    limit > 0 && any { it.type == Material.SHIELD } -> EnchantShieldPage(player)
                    else -> FinalPage(player)
                }
            }
    }

    private val npcs = XizumConfig.models.npcs

    private val undoButton = Button(npcs.undo) { if (currentPage !is InitialPage) goBack() }.apply {
        sound = Sound.BLOCK_AMETHYST_BLOCK_BREAK
        enableCooldown = false
    }.register()

    private val forwardButton = Button(npcs.forward) { currentPage?.nextPage?.let { addAndShowPage(it) } }.apply {
        sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP
        enableCooldown = false
    }.register()

    private val finishButton = Button(npcs.finish) {
        DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) {
            val cash = Cash.getCash(it)

            switchContext(SynchronizationContext.SYNC)

            if (PartyAPI.inParty(it) && partying.add(it)) return@schedule it.sendMessage("§cVocê está em uma party do mcMMO. Se concluir a criação do xizum, você será removido dela automaticamente.")
            val battle = it.battle!!

            if (!it.canPay(battle.options.sonecas)) return@schedule it.playSoundAndSendMessage(Sound.ENTITY_ITEM_BREAK,
                "§cVocê não tem sonecas suficientes mais. Ajuste o valor da aposta para concluir a partida.")
            if (cash < battle.options.cash) return@schedule it.playSoundAndSendMessage(Sound.ENTITY_ITEM_BREAK,
                "§cVocê não tem pesadelos suficientes mais. Ajuste o valor da aposta para concluir a partida.")

            it.sendMessage("${DreamXizum.PREFIX} Convide jogadores para a partida com o comando ${highlight("/xizum convidar <nick>")}.")
            Lobby.creatingBattle.remove(it)
            battle.addToBattle(it)
            fetch(it).destroy()
            partying.remove(it)
        }
    }.register()

    private val saveKitButton = Button(npcs.save) {
        DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) {
            val kits = Kit.fetchAll(it.uniqueId)
            switchContext(SynchronizationContext.SYNC)
            addAndShowPage(PlayerKitsPage(true, kits, it))
        }
    }.register()

    private val pages = mutableListOf<AbstractPage>()
    private val currentPage get() = pages.getOrNull(pages.lastIndex)
    private lateinit var user: Player

    fun addAndShowPage(page: AbstractPage) {
        with (finishButton.npc) { if (page is BettingPage) addViewer(user) }
        with (saveKitButton.npc) { if (page is FinalPage && user.battle!!.options.itemsType == BattleItems.CUSTOM_ITEMS) addViewer(user) else removeViewer(user) }
        with (undoButton.npc) { if (page !is InitialPage && user !in viewers) addViewer(user) }
        with (forwardButton.npc) { page.nextPage?.let { if (user !in viewers) addViewer(user) } ?: removeViewer(user) }
        currentPage?.let {
            it.hideTo(user)
            it.onForward()
        }
        with (page) {
            pages.add(this)
            showTo(user)
        }
    }

    private fun goBack() {
        currentPage?.let {
            pages.remove(it)
            it.destroy(user)
            it.onBack()
        }

        with (pages[pages.lastIndex]) {
            finishButton.npc.let { if (this is FinalPage) it.removeViewer(user) }
            saveKitButton.npc.let { if (this is FinalPage && user.battle!!.options.itemsType == BattleItems.CUSTOM_ITEMS) it.addViewer(user) else it.removeViewer(user) }
            with (forwardButton.npc) { nextPage?.let { if (user !in viewers) addViewer(user) } ?: removeViewer(user) }
            if (this is InitialPage) undoButton.npc.removeViewer(user)
            showTo(user)
            onReturn()
        }
    }

     fun destroy() {
         undoButton.npc.destroy()
         forwardButton.npc.destroy()
         saveKitButton.npc.destroy()
         finishButton.npc.destroy()
         pages.forEach { it.destroy(user) }
         instances.remove(user)
     }
}