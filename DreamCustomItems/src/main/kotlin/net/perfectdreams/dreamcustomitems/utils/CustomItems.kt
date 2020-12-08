package net.perfectdreams.dreamcustomitems.utils

import com.destroystokyo.paper.profile.ProfileProperty
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

object CustomItems {
    val IS_MICROWAVE_KEY = NamespacedKey(Bukkit.getPluginManager().getPlugin("DreamCustomItems")!!, "is_microwave")

    val RUBY = ItemStack(Material.PRISMARINE_SHARD)
        .meta<ItemMeta> {
            setCustomModelData(1)
            setDisplayName("§fRubí")
        }

    val HAMBURGER = ItemStack(Material.BREAD)
        .meta<ItemMeta> {
            setCustomModelData(1)
            setDisplayName("§fHamburguer")
        }

    val CUPCAKE = ItemStack(Material.COOKIE)
        .meta<ItemMeta> {
            setCustomModelData(1)
            setDisplayName("§fCupcake")
        }

    val MICROWAVE = ItemStack(Material.PLAYER_HEAD)
        .meta<SkullMeta> {
            setDisplayName("§fMicro-ondas")
            lore = listOf(
                "§7Um jeito de cozinhar suas comidas (e objetos)",
                "§7de forma mais rápida e segura!",
                "§7",
                "§cOBSERVAÇÃO: §7Não coloque coisas de ferro no micro-ondas!"
            )
            playerProfile = Bukkit.createProfile(UUID.fromString("32eb8a61-6e66-4dfd-874e-7481190014a9"))
                .apply {
                    this.setProperty(
                        ProfileProperty(
                            "textures",
                            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2RiNTE4MmY3ZDE5YTAzMjg1NjEwYzQ2MDg4OTU5Y2EyZTA3NzU1ZWI3MmIwYWQ0ZGYyOWY4NmY5NTk4ODhlIn19fQ=="
                        )
                    )
                }
            persistentDataContainer.set(IS_MICROWAVE_KEY, PersistentDataType.BYTE, 1)
        }

    fun checkIfRubyShouldDrop() = chance(0.1)
}