package net.perfectdreams.dreamxizum.gui.pages

import net.perfectdreams.dreamcore.utils.WrapperHologram
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.config.NPCModel
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.gui.buttons.Button
import net.perfectdreams.dreamxizum.gui.buttons.EnchantableButton
import net.perfectdreams.dreamxizum.gui.buttons.ToggleableButton
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

abstract class AbstractPage {
    internal val plugin = DreamXizum.INSTANCE
    internal val npcs = XizumConfig.models.npcs
    internal var hologram: WrapperHologram? = null
    private val buttons = mutableListOf<Button>()
    var nextPage: AbstractPage? = null

    /**
     * Creates a simple button
     */
    fun button(model: NPCModel, callback: (Player) -> Unit) = Button(model, callback).apply { buttons.add(this) }.register()

    /**
     * Creates a simple named button
     */
    fun namedButton(model: NPCModel, name: String, callback: (Player) -> Unit) = Button(model, callback).apply { this.name = name; buttons.add(this) }.register()

    /**
     * Creates a toggleable button
     */
    fun toggleableButton(model: NPCModel, callback: (Player, Boolean) -> Unit) = ToggleableButton(model, callback).apply { buttons.add(this) }.register()

    /**
     * Creates an enchantable button
     */
    fun enchantableButton(model: NPCModel, maxLevel: Int, callback: (Player, Int) -> Unit) = EnchantableButton(model, maxLevel, callback).apply { buttons.add(this) }.register()

    /**
     * Method is called when the user goes forward
     */
    open fun onForward() {}

    /**
     * Method is called when the user goes back from this page
     */
    open fun onBack() {}

    /**
     * Method is called when the user comes back to this page
     */
    open fun onReturn() {}

    /**
     * Shows page to player
     */
    fun showTo(player: Player) {
        buttons.forEach { it.npc.addViewer(player) }
        hologram?.addViewer(player)
    }

    /**
     * Hides page from player
     */
    fun hideTo(player: Player) {
        buttons.forEach { it.npc.removeViewer(player) }
        hologram?.removeViewer(player)
    }

    /**
     * Destroys page
     */
    fun destroy(player: Player) {
        buttons.forEach { it.npc.destroy() }
        hologram?.removeViewer(player)
    }

    internal class EnchantmentAndLevel(val enchantment: Enchantment, val maxLevel: Int)
}