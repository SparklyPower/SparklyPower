package net.perfectdreams.dreamsocial.gui.profile.settings.layout

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamsocial.DreamSocial
import net.perfectdreams.dreamsocial.dao.ProfileEntity
import net.perfectdreams.dreamsocial.gui.profile.ProfileLayout
import org.jetbrains.exposed.sql.transactions.transaction

fun renderLayoutMenu(plugin: DreamSocial, profileEntity: ProfileEntity) =
    createMenu(9, "ꈉ§f\uE266§r陇§fEscolha um layout") {
        val initialSlot = 3

        ProfileLayout.values().forEachIndexed { index, layout ->
            slot(index + initialSlot, 0) {
                item = layout.showcaseItem

                onClick { humanEntity ->
                    plugin.schedule(SynchronizationContext.ASYNC) {
                        transaction(Databases.databaseNetwork) {
                            profileEntity.layout = layout
                        }

                        switchContext(SynchronizationContext.SYNC)

                        humanEntity.closeInventory()

                        humanEntity.sendMessage("§aVocê atualizou o layout do seu perfil com sucesso ^-^")
                    }
                }
            }
        }
    }