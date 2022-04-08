package net.perfectdreams.dreamxizum.gui.pages.kits

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.dao.Kit
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Material
import org.bukkit.Sound

class KitsPage(val options: BattleOptions) : AbstractPage() {
    init {
        button(npcs.playerKit) {
            plugin.schedule(SynchronizationContext.ASYNC) {
                val kits = Kit.fetchAll(it.uniqueId)
                switchContext(SynchronizationContext.SYNC)
                if (kits.isEmpty()) return@schedule it.playSoundAndSendMessage(Sound.BLOCK_ANVIL_LAND,
                    "§cVocê não tem nenhum kit pré-definido. Monte um primeiro e então certifique-se de salvá-lo para que possa utilizar essa opção.")
                Paginator.fetch(it).addAndShowPage(PlayerKitsPage(false, kits, it))
                it.playSoundAndSendMessage(Sound.UI_BUTTON_CLICK, "${DreamXizum.PREFIX} Se você quiser excluir um kit, clique nele enquanto estiver agachad${it.artigo}.")
            }
        }.apply {
            with (npc) {
                changeItem(Material.SPYGLASS)
                isRightClicking = true
            }
            sound = null
        }

        button(npcs.pluginKit) {
            it.sendMessage("${DreamXizum.PREFIX} Se você quiser ver o conteúdo do kit, clique nele enquanto estiver agachad${it.artigo}.")
            Paginator.fetch(it).addAndShowPage(PluginKitsPage(options))
        }
    }

    override fun onBack() { options.clearItems() }
}