package net.perfectdreams.dreamenchant.utils

import net.perfectdreams.dreambedrockintegrations.utils.isBedrockClient
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamenchant.DreamEnchant
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import protocolsupport.api.TranslationAPI

abstract class SpecialEnchantmentTable {
    val rubiesToCreditsAmount = 1
    val pesadelosToCreditsAmount = 15L

    abstract val creditsAmount: Int

    abstract fun openEnchantmentInventoryOrCreditsScreen(player: Player, clickedBlock: Block, page: Int)

    abstract fun hasEnoughCreditsToPurchaseEnchantment(player: Player, block: Block): Boolean

    fun validateIfItemCanBeEnchanted(player: Player, item: ItemStack): Boolean {
        if (item.type == Material.AIR) {
            player.sendMessage("§cPegue o item que você deseja encantar!")
            return false
        }

        val enchants = Enchantment.values().filter { it.canEnchantItem(item) }
            .sortedByDescending { it.maxLevel }

        if (enchants.isEmpty()) {
            player.sendMessage("§cO item que está na sua mão não pode ser encantado!")
            return false
        }

        return true
    }

    fun createEnchantmentsMenu(
        m: DreamEnchant,
        player: Player,
        heldItem: ItemStack,
        clickedBlock: Block,
        page: Int,
        credits: Int,
        openEnchantmentInventoryBlock: (Player, Block, Int, Int) -> Unit,
        openEnchantmentCreditsInventoryBlock: (Player, Block, Int) -> Unit
    ): DreamMenu {
        val enchants = Enchantment.values()
            .filter { it.canEnchantItem(heldItem) }
            .sortedByDescending { it.maxLevel }

        return createMenu(54, "§fꈉ\uE253") {
            slot(8) {
                item = ItemStack(Material.PAPER)
                    .rename("§aCréditos: §e$credits")
                    .meta<ItemMeta> {
                        setCustomModelData(1)
                    }

                onClick {
                    player.closeInventory()
                    openEnchantmentCreditsInventoryBlock.invoke(player, clickedBlock, page)
                }
            }

            slot(53) {
                item = ItemStack(Material.BARRIER)
                    .rename("§c§lDeixa para lá...")

                onClick {
                    it.closeInventory()
                }
            }

            slot(52) {
                item = ItemStack(Material.ENCHANTING_TABLE)
                    .rename("§e§lUtilizar a velha mesa de encantamento")

                onClick {
                    player.openEnchanting(clickedBlock.location, false)
                }
            }

            if (page != 0) {
                // Voltar no menu
                slot(50) {
                    item = ItemStack(Material.PAPER)
                        .rename("§e§lVoltar página")
                        .meta<ItemMeta> {
                            setCustomModelData(7)
                        }

                    onClick {
                        player.closeInventory()
                        openEnchantmentInventoryBlock.invoke(
                            player,
                            clickedBlock,
                            page - 1,
                            credits
                        )
                    }
                }
            }

            if (enchants.size > (page + 1) * 6) {
                // Avançar no menu
                slot(51) {
                    item = ItemStack(Material.PAPER)
                        .meta<ItemMeta> {
                            setCustomModelData(8)
                        }
                        .rename("§e§lIr para a próxima página")

                    onClick {
                        player.closeInventory()
                        openEnchantmentInventoryBlock.invoke(
                            player,
                            clickedBlock,
                            page + 1,
                            credits
                        )
                    }
                }
            }

            for ((index, enchantment) in enchants.subList(page * 6, enchants.size.coerceAtMost((page + 1) * 6)).withIndex()) {
                for (level in enchantment.startLevel..enchantment.maxLevel) {
                    val localeKey = "enchantment.minecraft.${enchantment.key.key}"
                    val ptEnchantment = TranslationAPI.getTranslationString("pt_br", localeKey)
                    val usEnchantment = TranslationAPI.getTranslationString("en_us", localeKey)

                    val multiplier = m.getLevelMultiplierForPlayer(player)

                    slot((index * 9) + (level - 1)) {
                        item = ItemStack(
                            if (player.isBedrockClient) {
                                when (level) {
                                    1 -> Material.COAL_BLOCK
                                    2 -> Material.IRON_BLOCK
                                    3 -> Material.GOLD_BLOCK
                                    4 -> Material.DIAMOND_BLOCK
                                    5 -> Material.EMERALD_BLOCK
                                    else -> Material.OBSIDIAN
                                }
                            } else {
                                Material.PAPER
                            }
                        )
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
                            .meta<ItemMeta> {
                                setCustomModelData(level + 1) // In the resource pack, the levels are from 2 to 6
                            }

                        onClick {
                            player.closeInventory()

                            val hasConflict = heldItem.enchantments.any {
                                it.key != enchantment && it.key.conflictsWith(enchantment)
                            }

                            if (hasConflict) {
                                player.sendMessage("§cO encantamento que você quer aplicar tem conflito com outro encantamento que está no seu item!")
                                return@onClick
                            }

                            val levelCost = level * m.getLevelMultiplierForPlayer(player)

                            if (levelCost > player.level) {
                                player.sendMessage("§cVocê não possui experiência suficiente para encantar este item!")
                                return@onClick
                            }

                            val hasEnoughCredits = hasEnoughCreditsToPurchaseEnchantment(player, clickedBlock)

                            if (!hasEnoughCredits) {
                                player.sendMessage("§cVocê não possui créditos suficiente para encantar este item!")
                                return@onClick
                            }

                            heldItem.addEnchantment(enchantment, level)
                            player.level = (player.level - levelCost)
                            player.sendMessage("§aO seu item foi encantado com sucesso!")
                            player.world.spawnParticle(Particle.HAPPY_VILLAGER, player.location, 20, 1.0, 1.0, 1.0)
                            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        }
                    }
                }
            }
        }
    }

    fun createEnchantmentsCreditsMenu(
        m: DreamEnchant,
        player: Player,
        clickedBlock: Block,
        page: Int,
        creditsRedeemedBlock: (Player, Block) -> Unit
    ): DreamMenu {
        val menu = createMenu(9, "§fꈉ\uE254") {
            slot(3, 0) {
                item = CustomItems.RUBY
                    .clone()
                    .rename("§a${rubiesToCreditsAmount} rubí §6➤§a ${creditsAmount} créditos de encantamento")
                    .lore(
                        "§7Ao selecionar esta opção, ${rubiesToCreditsAmount} rubí será removido",
                        "§7do seu inventário e você receberá $creditsAmount",
                        "§7créditos de encantamento.",
                        "§7",
                        "§7Cada encantamento custa um crédito + a",
                        "§7quantidade de experiência necessária!"
                    )
                    .clone()

                onClick {
                    val rubyInTheInventory =
                        it.inventory.firstOrNull { it != null && it.type == Material.PRISMARINE_SHARD && it.hasItemMeta() && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData == 1 }

                    if (rubyInTheInventory == null) {
                        it.closeInventory()
                        player.sendMessage("§cVocê precisa ter um rubí no inventário para transformar ele em créditos de encantamento!")
                        return@onClick
                    }

                    rubyInTheInventory.amount -= 1

                    it.closeInventory()

                    // Increase the credits count
                    creditsRedeemedBlock.invoke(player, clickedBlock)

                    openEnchantmentInventoryOrCreditsScreen(player, clickedBlock, page)
                }
            }

            slot(5, 0) {
                item = ItemStack(Material.NETHER_STAR)
                    .meta<ItemMeta> {
                        setCustomModelData(1)
                    }
                    .rename("§a$pesadelosToCreditsAmount pesadelos §6➤§a $creditsAmount créditos de encantamento")
                    .lore(
                        "§7Ao selecionar esta opção, $pesadelosToCreditsAmount pesadelos serão",
                        "§7removidos e você receberá $creditsAmount",
                        "§7créditos de encantamento.",
                        "§7",
                        "§7Cada encantamento custa um crédito + a",
                        "§7quantidade de experiência necessária!"
                    )
                    .clone()

                onClick {
                    it as Player

                    m.launchAsyncThread {
                        val cash = Cash.getCash(it)

                        if (pesadelosToCreditsAmount > cash) {
                            onMainThread {
                                it.closeInventory()
                                player.sendMessage("§cVocê precisa ter $pesadelosToCreditsAmount pesadelos para transformar eles em créditos de encantamento!")
                            }
                            return@launchAsyncThread
                        }

                        Cash.takeCash(
                            it,
                            pesadelosToCreditsAmount,
                            TransactionContext(extra = "comprar créditos em uma super mesa de encantamento")
                        )

                        onMainThread {
                            it.closeInventory()

                            // Increase the credits count
                            creditsRedeemedBlock.invoke(player, clickedBlock)

                            openEnchantmentInventoryOrCreditsScreen(player, clickedBlock, page)
                        }
                    }
                }
            }

            slot(8, 0) {
                item = ItemStack(Material.ENCHANTING_TABLE)
                    .rename("§e§lUtilizar a velha mesa de encantamento")

                onClick {
                    it.openEnchanting(clickedBlock.location, false)
                }
            }
        }

        return menu
    }
}