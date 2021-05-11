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
    val IS_SUPERFURNACE_KEY = NamespacedKey(Bukkit.getPluginManager().getPlugin("DreamCustomItems")!!, "is_superfurnace")
    val IS_TRASHCAN_KEY = NamespacedKey(Bukkit.getPluginManager().getPlugin("DreamCustomItems")!!, "is_trashcan")

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

    val SUPERFURNACE = ItemStack(Material.PLAYER_HEAD).meta<SkullMeta> {
        setDisplayName("§fSuper fornalha")

        lore = listOf(
                "§7A fornalha mais rápida existente! Ela é",
                "§7perfeita para esquentar minérios.",
                "§7",
                "§cOBSERVAÇÃO: §7Apenas §b§lVIPs+§7, §b§lVIPs+ §7ou §b§lVIPs++ §7conseguem usar o equipamento!"
        )

        playerProfile = Bukkit.createProfile(UUID.fromString("c70bc8a2-61cb-46f8-955f-fd27026834f0")).apply {
            this.setProperty(
                    ProfileProperty(
                            "textures",
                            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI5NmYwOTI1MjRhZTljMmEyZTg3ODgxY2I0MTVhZGIzNThkNmNiNzczYzg1ZGM5NzIwMmZlZmI3NTRjMSJ9fX0="
                    )
            )
        }
        persistentDataContainer.set(IS_SUPERFURNACE_KEY, PersistentDataType.BYTE, 1)
    }

    val RAINBOW_WOOL = ItemStack(Material.WHITE_WOOL)
        .meta<ItemMeta> {
            setCustomModelData(1)
            // Lã Arco-Íris
            setDisplayName("§x§f§f§0§0§0§0L§x§f§f§8§0§0§0ã§x§f§f§f§f§0§0 §x§8§0§f§f§0§0A§x§0§0§f§f§0§0r§x§0§0§f§f§8§0c§x§0§0§f§f§f§fo§x§0§0§8§0§f§f-§x§0§0§0§0§f§fÍ§x§7§f§0§0§f§fr§x§f§f§0§0§f§fi§x§f§f§0§0§8§0s")
            lore = listOf("§7Também conhecido como \"Lã Gamer\"")
        }

    val TRASHCAN = ItemStack(Material.PLAYER_HEAD).meta<SkullMeta> {
        setDisplayName("§fLixeira")

        lore = listOf(
                "§7Lixeira para você que não quer fazer",
                "§7um buraco com lava para jogarem lixo!",
                "§7O mais legal dessa lixeira é que",
                "§7você não precisará esvaziar ela nunca!"
        )

        playerProfile = Bukkit.createProfile(UUID.fromString("28b9c4bd-ac62-43ee-a19c-eab295a73758")).apply {
            this.setProperty(
                    ProfileProperty(
                            "textures",
                            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWUwM2JmNTEzM2JkZTBlNmI4YzYyOTk3NTIxNWMyOGIwMDQ4MTIwYzQwZTNkNzM5MTIyYmEwOWNkMTc3OGNlYSJ9fX0="
                    )
            )
        }

        persistentDataContainer.set(IS_TRASHCAN_KEY, PersistentDataType.BYTE, 1)
    }

    fun checkIfRubyShouldDrop() = chance(0.1)
}
