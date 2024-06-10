package net.perfectdreams.dreamcustomitems.utils

import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.set
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

object CustomItems {
    val CUSTOM_ITEM_KEY = SparklyNamespacedKey("custom_item", PersistentDataType.STRING)
    val IS_MICROWAVE_KEY = NamespacedKey(Bukkit.getPluginManager().getPlugin("DreamCustomItems")!!, "is_microwave")
    val IS_SUPERFURNACE_KEY = NamespacedKey(Bukkit.getPluginManager().getPlugin("DreamCustomItems")!!, "is_superfurnace")
    val IS_TRASHCAN_KEY = NamespacedKey(Bukkit.getPluginManager().getPlugin("DreamCustomItems")!!, "is_trashcan")
    val RUBY_DROP_CHANCE = 0.1

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

    val PUDDING = ItemStack(Material.GOLDEN_CARROT)
        .meta<ItemMeta> {
            setCustomModelData(1)
            setDisplayName("§fPudim")
        }

    val FRENCH_FRIES = ItemStack(Material.BAKED_POTATO)
        .meta<ItemMeta> {
            this.setCustomModelData(1)
            setDisplayName("§fBatata Frita")
        }

    val PASTEL = ItemStack(Material.PUMPKIN_PIE)
        .meta<ItemMeta> {
            this.setCustomModelData(1)
            setDisplayName("§fPastel")
        }

    val COXINHA = ItemStack(Material.COOKED_RABBIT)
        .meta<ItemMeta> {
            this.setCustomModelData(1)
            setDisplayName("§fCoxinha")
        }

    val LORITTA_AND_PANTUFA_MUG = ItemStack(Material.PAPER)
        .meta<ItemMeta> {
            this.setCustomModelData(57)
            setDisplayName("§fCaneca da Loritta e da Pantufa")
        }

    val ANGEL_HALO = ItemStack(Material.PAPER)
        .meta<ItemMeta> {
            this.setCustomModelData(58)
            setDisplayName("§fAuréola de Anjo")
        }

    val ESTALINHO_RED = ItemStack(Material.PRISMARINE_SHARD)
        .meta<ItemMeta> {
            setCustomModelData(2)
            setDisplayName("§c§lEstalinho")

            lore = listOf(
                "§7Cuidado para não machucar a mão!"
            )
        }

    val ESTALINHO_GREEN = ItemStack(Material.PRISMARINE_SHARD)
        .meta<ItemMeta> {
            setCustomModelData(3)
            setDisplayName("§a§lEstalinho")

            lore = listOf(
                "§7Cuidado para não machucar a mão!"
            )
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
            "§cOBSERVAÇÃO: §7Apenas §b§lVIPs§7, §b§lVIPs+ §7ou §b§lVIPs++ §7conseguem usar o equipamento!"
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
            persistentDataContainer.set(CUSTOM_ITEM_KEY, "rainbow_wool")
            setCustomModelData(1)
            // Lã Arco-Íris
            setDisplayName("§x§f§f§0§0§0§0L§x§f§f§8§0§0§0ã§x§f§f§f§f§0§0 §x§8§0§f§f§0§0A§x§0§0§f§f§0§0r§x§0§0§f§f§8§0c§x§0§0§f§f§f§fo§x§0§0§8§0§f§f-§x§0§0§0§0§f§fÍ§x§7§f§0§0§f§fr§x§f§f§0§0§f§fi§x§f§f§0§0§8§0s")
            lore = listOf("§7Também conhecido como \"Lã Gamer\"")
        }

    val RAINBOW_CONCRETE = ItemStack(Material.WHITE_CONCRETE)
        .meta<ItemMeta> {
            persistentDataContainer.set(CUSTOM_ITEM_KEY, "rainbow_concrete")
            setCustomModelData(1)
            // Concreto Arco-Íris
            setDisplayName("§x§f§f§0§0§0§0C§x§f§f§5§5§0§0o§x§f§f§a§a§0§0n§x§f§f§f§f§0§0c§x§a§a§f§f§0§0r§x§5§5§f§f§0§0e§x§0§0§f§f§0§0t§x§0§0§f§f§5§5o§x§0§0§f§f§a§a §x§0§0§f§f§f§fA§x§0§0§a§a§f§fr§x§0§0§5§5§f§fc§x§0§0§0§0§f§fo§x§5§5§0§0§f§f-§x§a§a§0§0§f§fÍ§x§f§f§0§0§f§fr§x§f§f§0§0§a§ai§x§f§f§0§0§5§5s")
            lore = listOf("§7Também conhecido como \"Concreto Gamer\"")
        }

    val RAINBOW_TERRACOTTA = ItemStack(Material.WHITE_TERRACOTTA)
        .meta<ItemMeta> {
            persistentDataContainer.set(CUSTOM_ITEM_KEY, "rainbow_terracotta")
            setCustomModelData(1)
            // Terracota Arco-Íris
            setDisplayName("§x§f§f§0§0§0§0T§x§f§f§5§1§0§0e§x§f§f§a§1§0§0r§x§f§f§f§2§0§0r§x§b§c§f§f§0§0a§x§6§b§f§f§0§0c§x§1§b§f§f§0§0o§x§0§0§f§f§3§6t§x§0§0§f§f§8§6a§x§0§0§f§f§d§7 §x§0§0§d§7§f§fA§x§0§0§8§6§f§fr§x§0§0§3§6§f§fc§x§1§b§0§0§f§fo§x§6§b§0§0§f§f-§x§b§c§0§0§f§fÍ§x§f§f§0§0§f§2r§x§f§f§0§0§a§1i§x§f§f§0§0§5§1s")
            lore = listOf("§7Também conhecido como \"Terracota Gamer\"")
        }

    val ASPHALT_SERVER = ItemStack(Material.BLACK_CONCRETE)
        .meta<ItemMeta> {
            persistentDataContainer.set(CUSTOM_ITEM_KEY, "asphalt_server")
            setCustomModelData(1)
            displayNameWithoutDecorations {
                color(NamedTextColor.RED)
                content("Asfalto (Servidor)")
            }
        }

    val ASPHALT_PLAYER = ItemStack(Material.BLACK_CONCRETE)
        .meta<ItemMeta> {
            persistentDataContainer.set(CUSTOM_ITEM_KEY, "asphalt_player")
            setCustomModelData(1)
            displayNameWithoutDecorations {
                content("Asfalto")
            }
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

    val MAGNET = ItemStack(Material.STONE_HOE)
        .meta<ItemMeta> {
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            setCustomModelData(1)
            setDisplayName("§c§lÍmã")

            lore = listOf(
                "§7Graças aos seus polos §bmagnéticos§7, esse item atrai",
                "§7todo §cminério§7 que você minerar para o seu inventário.",
                "",
                "§6Pode atrair até §f4320 §6minérios"
            )
        }

    val MAGNET_2 = ItemStack(Material.STONE_HOE)
        .meta<ItemMeta> {
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            setCustomModelData(2)
            setDisplayName("§x§d§1§6§d§e§9§lÍmã grotesco")

            lore = listOf(
                "§7Ele atrai qualquer bloco que seja quebrado",
                "§7por você, sendo um minério ou não.",
                "§eBizarro§7, mas funciona.",
                "",
                "§6Pode atrair até §f8640 §6itens"
            )
        }

    val CHIMARRAO_EMPTY_BROWN = ItemStack(Material.PAPER)
        .meta<ItemMeta> {
            setCustomModelData(194)
            setDisplayName("§fCuia")
        }

    val CHIMARRAO_EMPTY_LORI_WHITE = ItemStack(Material.PAPER)
        .meta<ItemMeta> {
            setCustomModelData(195)
            setDisplayName("§fCuia")
        }

    val CHIMARRAO_EMPTY_LORI_BLACK = ItemStack(Material.PAPER)
        .meta<ItemMeta> {
            setCustomModelData(196)
            setDisplayName("§fCuia")
        }

    val CHIMARRAO_BROWN = ItemStack(Material.MILK_BUCKET)
        .meta<ItemMeta> {
            setCustomModelData(1)
            setDisplayName("§fChimarrão")
        }

    val CHIMARRAO_LORI_WHITE = ItemStack(Material.MILK_BUCKET)
        .meta<ItemMeta> {
            setCustomModelData(2)
            setDisplayName("§fChimarrão")
        }

    val CHIMARRAO_LORI_BLACK = ItemStack(Material.MILK_BUCKET)
        .meta<ItemMeta> {
            setCustomModelData(3)
            setDisplayName("§fChimarrão")
        }
}