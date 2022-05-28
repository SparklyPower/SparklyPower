package net.perfectdreams.dreamenchant.utils

import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import protocolsupport.api.TranslationAPI

// import protocolsupport.api.TranslationAPI

object EnchantUtils {
    fun getLevelMultiplierForPlayer(player: Player): Int {
        return when {
            player.hasPermission("dreamenchant.vip++") -> 4
            player.hasPermission("dreamenchant.vip+") -> 5
            player.hasPermission("dreamenchant.vip") -> 6
            else -> 7
        }
    }

    fun openEnchantmentInventory(player: Player, clickedBlock: Block, page: Int) {
        val heldItem = player.inventory.itemInMainHand

        if (heldItem.type == Material.AIR) {
            player.sendMessage("§cSegue o item que você deseja encantar!")
            return
        }

        val enchants = Enchantment.values().filter { it.canEnchantItem(heldItem) }
            .sortedByDescending { it.maxLevel }

        if (enchants.isEmpty()) {
            player.sendMessage("§cO item que está na sua mão não pode ser encantado!")
            return
        }

        val inventory = Bukkit.createInventory(EnchantHolder(clickedBlock), 54, "§a§lEscolha o Encantamento!")

        inventory.setItem(
            53,
            ItemStack(Material.BARRIER)
                .rename("§c§lDeixa para lá...")
                .storeMetadata("inventoryAction", "close")
        )

        inventory.setItem(
            52,
            ItemStack(Material.ENCHANTING_TABLE)
                .rename("§e§lUtilizar a velha mesa de encantamento")
                .storeMetadata("inventoryAction", "old")
        )

        if (page != 0) {
            // Voltar no menu
            inventory.setItem(
                50,
                ItemStack(Material.EMERALD_BLOCK)
                    .rename("§e§lVoltar página")
                    .storeMetadata("inventoryAction", "go")
                    .storeMetadata("newPage" , (page - 1).toString())
            )
        }

        if (enchants.size > (page + 1) * 6) {
            // Avançar no menu
            inventory.setItem(
                51,
                ItemStack(Material.EMERALD_BLOCK)
                    .rename("§e§lIr para a próxima página")
                    .storeMetadata("inventoryAction", "go")
                    .storeMetadata("newPage", (page + 1).toString())
            )
        }

        for ((index, enchant) in enchants.subList(page * 6, Math.min(enchants.size, (page + 1) * 6)).withIndex()) {
            for (level in enchant.startLevel..enchant.maxLevel) {
                val material = when (level) {
                    1 -> Material.COAL_BLOCK
                    2 -> Material.IRON_BLOCK
                    3 -> Material.GOLD_BLOCK
                    4 -> Material.DIAMOND_BLOCK
                    5 -> Material.EMERALD_BLOCK
                    else -> Material.OBSIDIAN
                }

                val localeKey = "enchantment.minecraft.${enchant.key.key}"
                val ptEnchantment = TranslationAPI.getTranslationString("pt_br", localeKey)
                val usEnchantment = TranslationAPI.getTranslationString("en_us", localeKey)

                val multiplier = getLevelMultiplierForPlayer(player)

                inventory.setItem(
                    (index * 9) + (level - 1),
                    ItemStack(material)
                        .rename(
                            "§a$usEnchantment§8/§a$ptEnchantment §7${level.toRomanNumeral()} §8[§e§l${level * multiplier} §a§lníveis§8]"
                        )
                        .apply {
                            if (multiplier != 7) {
                                this.lore(
                                    "§aPreço normal: §e§6§l${level * 7} níveis§e",
                                    "§7",
                                    "§7VIPs ganham desconto em encantamentos!"
                                )
                            } else {
                                this.lore(
                                    "§aPreço para VIPs: §eSomente §6§l" + level * 6 + " níveis§e!",
                                    "§aPreço para VIPs+: §eSomente §6§l" + level * 5 + " níveis§e!",
                                    "§aPreço para VIPs++: §eSomente §6§l" + level * 4 + " níveis§e!",
                                    "§7",
                                    "§7Gostou do preço? Então compre VIP! §e/ajuda vip"
                                )
                            }
                        }
                        .storeMetadata("enchantWith", enchant.name)
                        .storeMetadata("enchantmentLevel", level.toString())
                        .storeMetadata("itemInHandHash", heldItem.hashCode().toString())
                        .storeMetadata("inventoryAction", "enchant")
                )
            }
        }

        player.openInventory(inventory)
    }
}