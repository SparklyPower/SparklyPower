package net.perfectdreams.dreamsocial.gui.profile.settings.preferences

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.dao.PreferencesEntity
import net.perfectdreams.dreamcore.utils.adventure.lore
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import net.perfectdreams.dreamsocial.DreamSocial
import net.perfectdreams.dreamsocial.gui.confirmation.getConfirmationButton
import net.perfectdreams.dreamsocial.gui.profile.settings.preferences.helper.item.item
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta

private val orderedPreferences = listOf(BroadcastType.PRIVATE_MESSAGE, BroadcastType.CHAT_EVENT, BroadcastType.PLAYER_ANNOUNCEMENT,
    BroadcastType.SERVER_ANNOUNCEMENT, BroadcastType.EVENT_ANNOUNCEMENT, BroadcastType.LOGIN_ANNOUNCEMENT, BroadcastType.JETPACK_MESSAGE,
    BroadcastType.GAMBLING_MESSAGE, BroadcastType.VOTES_MESSAGE, BroadcastType.THANOS_SNAP)

fun renderPreferencesMenu(plugin: DreamSocial, preferencesEntity: PreferencesEntity, player: Player) =
    createMenu(45, "ꈉ§f" + if (player.isStaff) '\uE271' else '\uE270') {
        var isBusy = false

        orderedPreferences.forEachIndexed { index, broadcastType ->
            if (broadcastType == BroadcastType.THANOS_SNAP && !player.isStaff) return@forEachIndexed

            val firstX = if (index < 5) 2 else 6
            val firstY = index % 5

            slot(firstX, firstY) { item = broadcastType.item() }

            val secondX = firstX + if (index < 5) 1 else -1

            slot(secondX, firstY) {
                var isActivated = preferencesEntity[broadcastType] && player.highestRole >= broadcastType.minimumRoleToDisable

                item = getActivationButton(isActivated)

                onClick {
                    if (isBusy) return@onClick

                    isBusy = true

                    if (player.highestRole < broadcastType.minimumRoleToDisable) {
                        it.sendMessage(
                            "Você precisa ser, no mínimo, ${broadcastType.minimumRoleToDisable.localizedName} para desativar este tipo de mensagem."
                                .asComponent.color(NamedTextColor.RED)
                        )

                        return@onClick it.closeInventory()
                    }

                    plugin.schedule(SynchronizationContext.ASYNC) {
                        preferencesEntity.flip(broadcastType)

                        waitFor(20 * 5)

                        switchContext(SynchronizationContext.SYNC)

                        isActivated = !isActivated

                        it.openInventory.setItem(secondX + firstY * 9, getActivationButton(isActivated))

                        isBusy = false
                    }
                }
            }
        }
    }

private fun getActivationButton(isActivated: Boolean) =
    getConfirmationButton(isActivated, "Ativado" to "Desativado").meta<ItemMeta> {
        lore {
            textWithoutDecorations {
                color(NamedTextColor.GRAY)
                content("Clique para ${if (isActivated) "desativar" else "ativar"} este")
            }

            textWithoutDecorations {
                color(NamedTextColor.GRAY)
                content("tipo de mensagem.")
            }
        }
    }