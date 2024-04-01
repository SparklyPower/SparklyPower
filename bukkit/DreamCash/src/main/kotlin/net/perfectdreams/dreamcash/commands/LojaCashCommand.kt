package net.perfectdreams.dreamcash.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.InheritanceNode
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcash.DreamCash
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamclubes.tables.ClubeHomeUpgrades
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamloja.DreamLoja
import net.perfectdreams.dreamloja.tables.ShopWarpUpgrades
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class LojaCashCommand(val m: DreamCash) : SparklyCommand(arrayOf("lojacash", "cashloja")) {
    @Subcommand
    fun root(sender: Player) {
        showShopMenu(sender)
    }

    fun showShopMenu(sender: Player) {
        val lojaUpgradeCount = transaction(Databases.databaseNetwork) {
            ShopWarpUpgrades.select {
                ShopWarpUpgrades.playerId eq sender.uniqueId
            }.count()
        }
        val lojaUpgradePrice = (lojaUpgradeCount + 1) * 400

        val menu = createMenu(54, "§fꈉ\ue261") {
            fun generateItemAt(x: Int, y: Int, type: Material, customModelData: Int? = null, name: String, quantity: Long, callback: () -> (Boolean)) {
                slot(x, y) {
                    item = ItemStack(type)
                        .rename(name)
                        .lore(
                            "§c$quantity pesadelos"
                        )
                        .meta<ItemMeta> {
                            setCustomModelData(customModelData)
                        }

                    onClick {
                        checkIfPlayerHasSufficientMoney(sender, quantity) {
                            InventoryUtils.askForConfirmation(
                                sender,
                                afterAccept = {
                                    sender.closeInventory()

                                    scheduler().schedule(m, SynchronizationContext.SYNC) {
                                        val result = callback.invoke()

                                        if (result) {
                                            switchContext(SynchronizationContext.ASYNC)

                                            transaction(Databases.databaseNetwork) {
                                                try {
                                                    Cash.takeCash(sender, quantity, TransactionContext(extra = "comprar `$name` no `/lojacash`"))
                                                } catch (e: IllegalArgumentException) {
                                                    sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                                                    return@transaction
                                                }
                                            }

                                            switchContext(SynchronizationContext.SYNC)

                                            sender.sendMessage("§aObrigado pela compra! ^-^")

                                            Bukkit.broadcastMessage("${DreamCash.PREFIX} §b${sender.displayName}§a comprou $name§a na loja de §cpesadelos§a (§6/lojacash§a), agradeça por ter ajudado a manter o §4§lSparkly§b§lPower§a online! ^-^")
                                        }
                                    }
                                },
                                afterDecline = {
                                    it.closeInventory()
                                    showShopMenu(sender)
                                }
                            )
                        }
                    }
                }
            }

            // VIPs
            val isVipPlusPlus = sender.hasPermission("group.vip++")
            val isVipPlus = sender.hasPermission("group.vip+")
            val isVip = sender.hasPermission("group.vip")
            val hasAnyVip = isVip || isVipPlus || isVipPlusPlus

            // Accumulate means "add more time", this is to avoid issues when giving the VIP group ;)
            generateItemAt(0, 0, Material.IRON_INGOT, 1, "§b§lVIP §7(um mês • R$ 14,99)", 500) {
                if (hasAnyVip && !isVip) {
                    sender.sendMessage("§cVocê não pode alterar o seu VIP atual enquanto você já tem outro VIP ativo!")
                    false
                } else {
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "lp user ${sender.name} parent addtemp vip 32d accumulate"
                    )
                    true
                }
            }
            generateItemAt(1, 0, Material.GOLD_INGOT, 1, "§b§lVIP§e+ §7(um mês • R$ 29,99)", 1_000) {
                if (hasAnyVip && !isVipPlus) {
                    sender.sendMessage("§cVocê não pode alterar o seu VIP atual enquanto você já tem outro VIP ativo!")
                    false
                } else {
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "lp user ${sender.name} parent addtemp vip+ 32d accumulate"
                    )
                    true
                }
            }
            generateItemAt(2, 0, Material.DIAMOND, 1, "§b§lVIP§e++ §7(um mês • R$ 44,99)", 1_500) {
                if (hasAnyVip && !isVipPlusPlus) {
                    sender.sendMessage("§cVocê não pode alterar o seu VIP atual enquanto você já tem outro VIP ativo!")
                    false
                } else {
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "lp user ${sender.name} parent addtemp vip++ 32d accumulate"
                    )
                    true
                }
            }

            // Blocos de Proteção
            generateItemAt(0, 1, Material.DIRT, null, "§e2500 blocos de proteção", 65) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 2500")
                true
            }
            generateItemAt(1, 1, Material.MYCELIUM, null, "§e5000 blocos de proteção", 125) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 5000")
                true
            }
            generateItemAt(2, 1, Material.GRASS_BLOCK, null, "§e10000 blocos de proteção", 250) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 10000")
                true
            }

            // ===[ CLUBES ]===
            this.slot(0, 2) {
                item = ItemStack(Material.ARMOR_STAND)
                    .rename("§aAumentar o seu Clube em +1 slot para Membros")
                    .lore(
                        "§aPermita que o seu clube tenha mais pessoas! (Máximo: 32 membros)",
                        "§aLembre-se: Isto apenas afeta o seu clube atual, se você deletar",
                        "§ao seu clube, você irá perder os slots adicionais!",
                        "§f",
                        "§c100 pesadelos"
                    )
                    .meta<ItemMeta> {
                        setCustomModelData(1)
                    }

                onClick {
                    checkIfPlayerHasSufficientMoney(sender, 100) {
                        InventoryUtils.askForConfirmation(
                            sender,
                            afterAccept = {
                                sender.closeInventory()

                                scheduler().schedule(m, SynchronizationContext.ASYNC) {
                                    val clube = ClubeAPI.getPlayerClube(sender)

                                    switchContext(SynchronizationContext.SYNC)

                                    if (clube == null) {
                                        sender.sendMessage("§cVocê não possui um clube!")
                                        return@schedule
                                    }

                                    if (clube.ownerId != sender.uniqueId) {
                                        sender.sendMessage("§cApenas o dono do clube pode aumentar os slots!")
                                        return@schedule
                                    }

                                    if (clube.maxMembers >= 32) {
                                        sender.sendMessage("§cVocê já comprou todos os slots disponíveis!")
                                        return@schedule
                                    }

                                    switchContext(SynchronizationContext.ASYNC)

                                    transaction(Databases.databaseNetwork) {
                                        try {
                                            Cash.takeCash(sender, 100, TransactionContext(extra = "comprar `slots adicionais para o clube` no `/lojacash`"))
                                            clube.maxMembers++
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                                            return@transaction
                                        }
                                    }

                                    switchContext(SynchronizationContext.SYNC)

                                    sender.sendMessage("§aObrigado pela compra! ^-^")

                                    Bukkit.broadcastMessage("${DreamCash.PREFIX} §b${sender.displayName}§a comprou §dslots adicionais para o clube§a na loja de §cpesadelos§a (§6/lojacash§a), agradeça por ter ajudado a manter o §4§lSparkly§b§lPower§a online! ^-^")
                                }
                            },
                            afterDecline = {
                                it.closeInventory()
                                showShopMenu(sender)
                            }
                        )
                    }
                }
            }

            this.slot(1, 2) {
                item = ItemStack(Material.ARMOR_STAND)
                    .rename("§aAumentar o seu Clube em +1 slot para Casas")
                    .lore(
                        "§aPermita que o seu clube tenha mais casas! (Máximo: 5 casas)",
                        "§aLembre-se: Isto apenas afeta o seu clube atual, se você deletar",
                        "§ao seu clube, você irá perder os slots adicionais!",
                        "§f",
                        "§c250 pesadelos"
                    )
                    .meta<ItemMeta> {
                        setCustomModelData(1)
                    }

                onClick {
                    checkIfPlayerHasSufficientMoney(sender, 250) {
                        InventoryUtils.askForConfirmation(
                            sender,
                            afterAccept = {
                                sender.closeInventory()

                                scheduler().schedule(m, SynchronizationContext.ASYNC) {
                                    val clube = ClubeAPI.getPlayerClube(sender)

                                    switchContext(SynchronizationContext.SYNC)

                                    if (clube == null) {
                                        sender.sendMessage("§cVocê não possui um clube!")
                                        return@schedule
                                    }

                                    if (clube.ownerId != sender.uniqueId) {
                                        sender.sendMessage("§cApenas o dono do clube pode aumentar os slots!")
                                        return@schedule
                                    }

                                    switchContext(SynchronizationContext.ASYNC)

                                    val totalUpgrades = transaction(Databases.databaseNetwork) {
                                        ClubeHomeUpgrades.select {
                                            ClubeHomeUpgrades.clube eq clube.id
                                        }.count()
                                    }

                                    if (totalUpgrades == 5L) {
                                        sender.sendMessage("§cVocê já comprou todos os upgrades de casa disponíveis!")
                                        return@schedule
                                    }

                                    transaction(Databases.databaseNetwork) {
                                        try {
                                            Cash.takeCash(sender, 250, TransactionContext(extra = "comprar `slots adicionais para casas do clube` no `/lojacash`"))
                                            ClubeHomeUpgrades.insert {
                                                it[ClubeHomeUpgrades.clube] = clube.id
                                                it[ClubeHomeUpgrades.boughtAt] = Instant.now()
                                            }
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                                            return@transaction
                                        }
                                    }

                                    switchContext(SynchronizationContext.SYNC)

                                    sender.sendMessage("§aObrigado pela compra! ^-^")

                                    Bukkit.broadcastMessage("${DreamCash.PREFIX} §b${sender.displayName}§a comprou §dslots adicionais para casas do clube§a na loja de §cpesadelos§a (§6/lojacash§a), agradeça por ter ajudado a manter o §4§lSparkly§b§lPower§a online! ^-^")
                                }
                            },
                            afterDecline = {
                                it.closeInventory()
                                showShopMenu(sender)
                            }
                        )
                    }
                }
            }

            // ===[ LOJA ]===
            this.slot(0, 3) {
                item = ItemStack(Material.ARMOR_STAND)
                    .rename("§aAumentar as warps da sua loja em +1 warp")
                    .lore(
                        "§aPermita que o seu clube tenha mais pessoas! (Máximo: ${DreamLoja.MEMBER_MAX_SLOTS} warps)",
                        "§f",
                        "§c${lojaUpgradePrice} pesadelos"
                    )
                    .meta<ItemMeta> {
                        setCustomModelData(1)
                    }

                onClick {
                    checkIfPlayerHasSufficientMoney(sender, lojaUpgradePrice) {
                        InventoryUtils.askForConfirmation(
                            sender,
                            afterAccept = {
                                sender.closeInventory()

                                scheduler().schedule(m, SynchronizationContext.ASYNC) {
                                    val count = transaction(Databases.databaseNetwork) {
                                        ShopWarpUpgrades.select {
                                            ShopWarpUpgrades.playerId eq sender.uniqueId
                                        }.count()
                                    }

                                    switchContext(SynchronizationContext.SYNC)

                                    if (count >= DreamLoja.MAX_SLOT_UPGRADE_SLOTS) {
                                        sender.sendMessage("§cVocê já comprou todos os slots disponíveis!")
                                        return@schedule
                                    }

                                    switchContext(SynchronizationContext.ASYNC)

                                    transaction(Databases.databaseNetwork) {
                                        try {
                                            Cash.takeCash(sender, lojaUpgradePrice, TransactionContext(extra = "comprar `slots adicionais para a warps de loja` no `/lojacash`"))

                                            ShopWarpUpgrades.insert {
                                                it[ShopWarpUpgrades.playerId] = sender.uniqueId
                                                it[ShopWarpUpgrades.boughtAt] = Instant.now()
                                            }
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                                            return@transaction
                                        }
                                    }

                                    switchContext(SynchronizationContext.SYNC)

                                    sender.sendMessage("§aObrigado pela compra! ^-^")

                                    Bukkit.broadcastMessage("${DreamCash.PREFIX} §b${sender.displayName}§a comprou §dslots adicionais para warps de loja§a na loja de §cpesadelos§a (§6/lojacash§a), agradeça por ter ajudado a manter o §4§lSparkly§b§lPower§a online! ^-^")
                                }
                            },
                            afterDecline = {
                                it.closeInventory()
                                showShopMenu(sender)
                            }
                        )
                    }
                }
            }

            // Special stuff
            this.slot(7, 5) {
                item = ItemStack(Material.NETHER_STAR)
                    .rename("§aComo conseguir pesadelos?")
                    .meta<ItemMeta> {
                        setCustomModelData(1)
                    }

                onClick {
                    it.closeInventory()

                    sender.sendMessage("§aExistem vários jeitos de conseguir §cpesadelos§a!")
                    sender.sendMessage("§8• §eNa nossa loja§b https://sparklypower.net/loja")
                    sender.sendMessage("§8• §eVotando no servidor §6/votar")
                    sender.sendMessage("§8• §eVencendo eventos no servidor")
                }
            }

            this.slot(8, 5) {
                item = ItemStack(Material.BARRIER)
                    .rename("§c§lFechar menu")

                onClick {
                    it.closeInventory()
                }
            }

            this.slot(6,  5) {
                item = ItemStack(Material.BEACON)
                    .rename("§e§lInformações sobre o meu VIP ativo")

                onClick {
                    it.closeInventory()

                    val api = LuckPermsProvider.get()
                    val user = api.userManager.getUser(sender.uniqueId) ?: return@onClick
                    val vipPlusPlusPermission = user.nodes.filterIsInstance<InheritanceNode>()
                        .firstOrNull { it.groupName == "vip++" }
                    val vipPlusPermission = user.nodes.filterIsInstance<InheritanceNode>()
                        .firstOrNull { it.groupName == "vip+" }
                    val vipPermission = user.nodes.filterIsInstance<InheritanceNode>()
                        .firstOrNull { it.groupName == "vip" }

                    val time = vipPlusPlusPermission?.expiry ?: vipPlusPermission?.expiry ?: vipPermission?.expiry

                    if (time == null) {
                        sender.sendMessage("§cVocê não tem nenhum VIP ativo!")
                    } else {
                        sender.sendMessage("§eSeu VIP irá expirar em §6${DateUtils.formatDateDiff(time.toEpochMilli())}")
                    }
                }
            }
        }

        menu.sendTo(sender)
    }

    fun checkIfPlayerHasSufficientMoney(sender: Player, quantity: Long, callback: () -> (Unit)) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val cash = transaction(Databases.databaseNetwork) {
                Cash.getCash(sender)
            }

            switchContext(SynchronizationContext.SYNC)

            if (quantity > cash) {
                sender.closeInventory()
                sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                return@schedule
            }

            callback.invoke()
        }
    }
}