package net.perfectdreams.dreamxizum.gui.pages.betting

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.Formatter
import net.perfectdreams.dreamcore.utils.WrapperHologram
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Sound
import org.bukkit.entity.Player

class AdjustCashPage(val player: Player) : AbstractPage() {
    private val options = player.battle!!.options
    private val values = listOf(-100, -10, 10, 100)

    init {
        hologram = WrapperHologram(XizumConfig.models.locations.pageHologram.toBukkitLocation(), mutableListOf(
            "§x§1§e§c§b§e§1§lTotal apostado:", hologramLine)
        )

        with(npcs) { listOf(remove100, remove10, add10, add100) }.forEachIndexed { index, model ->
            button(model) {
                plugin.schedule(SynchronizationContext.ASYNC) {
                    val cash = Cash.getCash(it)
                    switchContext(SynchronizationContext.SYNC)
                    with (values[index]) {
                        if (this > 0) {
                            if (cash >= options.cash + this) options.cash += this
                            else it.playSoundAndSendMessage(Sound.ENTITY_ITEM_BREAK, "§cVocê não tem pesadelos suficientes para apostar.")
                        } else options.cash += this

                        hologram!!.setLine(1, hologramLine)
                    }
                }
            }.apply {
                enableCooldown = false
                sound = null
            }
        }
    }

    private val hologramLine get() = "§x§a§9§e§b§f§4§l${Formatter.formatMoney(options.cash.toInt(), 2)} §x§1§e§c§b§e§1§lpesadelos"
}