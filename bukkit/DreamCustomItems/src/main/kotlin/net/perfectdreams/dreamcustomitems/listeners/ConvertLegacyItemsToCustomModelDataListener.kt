package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

class ConvertLegacyItemsToCustomModelDataListener(val m: DreamCustomItems) : Listener {
    private val legacyItemsConverter = listOf(
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                1
            ),
            NewItem(
                Material.PAPER,
                133
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                2
            ),
            NewItem(
                Material.PAPER,
                134
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                3
            ),
            NewItem(
                Material.PAPER,
                135
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                4
            ),
            NewItem(
                Material.PAPER,
                136
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                5
            ),
            NewItem(
                Material.PAPER,
                137
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                6
            ),
            NewItem(
                Material.PAPER,
                138
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                7
            ),
            NewItem(
                Material.PAPER,
                139
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                8
            ),
            NewItem(
                Material.PAPER,
                140
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                9
            ),
            NewItem(
                Material.PAPER,
                141
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                10
            ),
            NewItem(
                Material.PAPER,
                142
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                11
            ),
            NewItem(
                Material.PAPER,
                143
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                12
            ),
            NewItem(
                Material.PAPER,
                144
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                13
            ),
            NewItem(
                Material.PAPER,
                145
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                14
            ),
            NewItem(
                Material.PAPER,
                146
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                15
            ),
            NewItem(
                Material.PAPER,
                147
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                16
            ),
            NewItem(
                Material.PAPER,
                148
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                17
            ),
            NewItem(
                Material.PAPER,
                149
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                18
            ),
            NewItem(
                Material.PAPER,
                150
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                19
            ),
            NewItem(
                Material.PAPER,
                151
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                20
            ),
            NewItem(
                Material.PAPER,
                152
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                21
            ),
            NewItem(
                Material.PAPER,
                153
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                22
            ),
            NewItem(
                Material.PAPER,
                154
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                23
            ),
            NewItem(
                Material.PAPER,
                155
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                24
            ),
            NewItem(
                Material.PAPER,
                156
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                25
            ),
            NewItem(
                Material.PAPER,
                157
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                26
            ),
            NewItem(
                Material.PAPER,
                158
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                27
            ),
            NewItem(
                Material.PAPER,
                159
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                28
            ),
            NewItem(
                Material.PAPER,
                160
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                29
            ),
            NewItem(
                Material.PAPER,
                161
            )
        ),
        LegacyItemInfo(
            LegacyItem(
                Material.WOODEN_HOE,
                30
            ),
            NewItem(
                Material.PAPER,
                162
            )
        ),
    )

    @EventHandler
    fun onHoeInteract(e: PlayerInteractEvent) {
        val itemInMainHand = e.item ?: return

        if (itemInMainHand.type != Material.WOODEN_HOE)
            return

        val meta = itemInMainHand.itemMeta

        if (meta.hasCustomModelData()) {
            // The new kind of custom items with custom model data, but they are still using wooden hoes
            // So let's just cancel the event and bye bye!
            e.isCancelled = true
            return
        }

        // This item does NOT have a custom model data, but has the unbreakable tag!
        if (meta.isUnbreakable) {
            if ((meta as Damageable).damage == 0) {
                // This means that the user has fixed the hoe at some point, and this shouldn't be possible, so let's remove the hoe from their inventory
                itemInMainHand.amount--
                return
            } else {
                // Just cancel the event because it is probably a custom item...
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        val itemInMainHand = e.item ?: return
        if (!itemInMainHand.hasItemMeta())
            return

        if (!itemInMainHand.itemMeta.isUnbreakable)
            return

        val legacyItemInfo = legacyItemsConverter.firstOrNull {
            it.oldData.type == itemInMainHand.type && (itemInMainHand.itemMeta as? Damageable)?.damage == it.oldData.damage
        } ?: return

        // Cancel the interaction since we will convert the item
        e.isCancelled = true

        itemInMainHand.type = legacyItemInfo.newData.type
        itemInMainHand.meta<Damageable> {
            setCustomModelData(legacyItemInfo.newData.customModelData)
            isUnbreakable = false
        }
    }

    data class LegacyItemInfo(
        val oldData: LegacyItem,
        val newData: NewItem
    )

    data class LegacyItem(
        val type: Material,
        val damage: Int
    )

    data class NewItem(
        val type: Material,
        val customModelData: Int
    )
}
