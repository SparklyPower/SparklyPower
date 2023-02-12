package net.perfectdreams.dreamsocial.gui.profile.settings

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.dao.PreferencesEntity
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.asBoldComponent
import net.perfectdreams.dreamcore.utils.extensions.asComponent
import net.perfectdreams.dreamcore.utils.extensions.isStaff
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamsocial.DreamSocial
import net.perfectdreams.dreamsocial.dao.ProfileEntity
import net.perfectdreams.dreamsocial.gui.profile.settings.layout.renderLayoutMenu
import net.perfectdreams.dreamsocial.gui.profile.settings.preferences.renderPreferencesMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun renderSettingsMenu(plugin: DreamSocial, player: Player) = createMenu(9, "ꈉ§f\uE266陇Configurações") {
    slot(3, 0) {
        item = ItemStack(Material.PAPER).meta<ItemMeta> {
            displayName("Editar layout".asBoldComponent.color { 0x0bbdf4 })
            setCustomModelData(129)
        }

        onClick {
            plugin.schedule(SynchronizationContext.ASYNC) {
                val profileEntity = ProfileEntity.fetch(player.uniqueId)

                switchContext(SynchronizationContext.SYNC)

                renderLayoutMenu(plugin, profileEntity).sendTo(player)
            }
        }
    }

    slot(4, 0) {
        item = ItemStack(Material.PAPER).meta<ItemMeta> {
            displayName(" ".asComponent)
            setCustomModelData(125)
        }
    }

    slot(5, 0) {
        item = ItemStack(Material.PAPER).meta<ItemMeta> {
            displayName("Preferências".asBoldComponent.color { 0x4d78b2 })
            setCustomModelData(131)
        }

        onClick {
            plugin.schedule(SynchronizationContext.ASYNC) {
                val preferencesEntity = PreferencesEntity.fetch(player.uniqueId)

                switchContext(SynchronizationContext.SYNC)

                renderPreferencesMenu(plugin, preferencesEntity, player).sendTo(player)
            }
        }
    }
}