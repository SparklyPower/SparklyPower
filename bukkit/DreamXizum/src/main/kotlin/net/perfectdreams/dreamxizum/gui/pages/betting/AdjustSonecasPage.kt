package net.perfectdreams.dreamxizum.gui.pages.betting

import net.perfectdreams.dreamcore.utils.Formatter
import net.perfectdreams.dreamcore.utils.WrapperHologram
import net.perfectdreams.dreamcore.utils.canPay
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Sound
import org.bukkit.entity.Player

class AdjustSonecasPage(val player: Player) : AbstractPage() {
    private val options = player.battle!!.options
    private val values = listOf(-100000, -10000, 10000, 100000)

    init {
        hologram = WrapperHologram(XizumConfig.models.locations.pageHologram.toBukkitLocation(), mutableListOf(
            "§x§1§e§c§b§e§1§lTotal apostado:", hologramLine)
        )

        with (npcs) { listOf(remove100k, remove10k, add10k, add100k) }.forEachIndexed { index, model ->
            button(model) {
                with (values[index]) {
                    if (this > 0) {
                        if (it.canPay(options.sonecas + this)) options.sonecas += this
                        else it.playSoundAndSendMessage(Sound.ENTITY_ITEM_BREAK, "§cVocê não tem sonecas suficientes para apostar.")
                    } else options.sonecas += this

                    hologram!!.setLine(1, hologramLine)
                }
            }.apply {
                enableCooldown = false
                sound = null
            }
        }
    }

    private val hologramLine get() = "§x§a§9§e§b§f§4§l${Formatter.formatMoney(options.sonecas.toInt())} §x§1§e§c§b§e§1§lsonecas"
}