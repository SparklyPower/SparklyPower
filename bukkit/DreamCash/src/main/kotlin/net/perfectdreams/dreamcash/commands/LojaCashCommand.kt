package net.perfectdreams.dreamcash.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.types.InheritanceNode
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcash.DreamCash
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.exposed.sql.transactions.transaction

class LojaCashCommand(val m: DreamCash) : SparklyCommand(arrayOf("lojacash", "cashloja")) {
    @Subcommand
    fun root(sender: Player) {
        showShopMenu(sender)
    }

    fun showShopMenu(sender: Player) {
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
                            askForConfirmation(sender) {
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
                            }
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
            generateItemAt(1, 0, Material.GOLD_INGOT, 1, "§b§lVIP§e+ §7(um mês • R$ 29,99)", 1000) {
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
            generateItemAt(2, 0, Material.DIAMOND, 1, "§b§lVIP§e++ §7(um mês • R$ 44,99)", 1500) {
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
            generateItemAt(0, 1, Material.DIRT, null, "§e8000 blocos de proteção", 15) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 8000")
                true
            }
            generateItemAt(1, 1, Material.MYCELIUM, null, "§e16000 blocos de proteção", 30) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 16000")
                true
            }
            generateItemAt(2, 1, Material.GRASS_BLOCK, null, "§e24000 blocos de proteção", 45) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 24000")
                true
            }

            // Money
            generateItemAt(0, 2, Material.EMERALD, 1, "§a130000 Sonecas", 250) {
                sender.deposit(130000.00, TransactionContext(extra = "comprar no `/lojacash`"))
                true
            }

            generateItemAt(1, 2, Material.EMERALD, 1, "§a260000 Sonecas", 500) {
                sender.deposit(260000.00, TransactionContext(extra = "comprar no `/lojacash`"))
                true
            }

            generateItemAt(2, 2, Material.EMERALD, 1, "§a500000 Sonecas", 950) {
                sender.deposit(500000.00, TransactionContext(extra = "comprar no `/lojacash`"))
                true
            }

            generateItemAt(3, 2, Material.EMERALD, 1, "§a1000000 Sonecas", 1_900) {
                sender.deposit(1000000.00, TransactionContext(extra = "comprar no `/lojacash`"))
                true
            }

            generateItemAt(4, 2, Material.EMERALD, 1, "§a2000000 Sonecas", 3_800) {
                sender.deposit(2000000.00, TransactionContext(extra = "comprar no `/lojacash`"))
                true
            }

            generateItemAt(5, 2, Material.EMERALD, 1, "§a5000000 Sonecas", 9_500) {
                sender.deposit(5000000.00, TransactionContext(extra = "comprar no `/lojacash`"))
                true
            }

            generateItemAt(6, 2, Material.EMERALD, 1, "§a10000000 Sonecas", 19_000) {
                sender.deposit(10000000.00, TransactionContext(extra = "comprar no `/lojacash`"))
                true
            }

            this.slot(0, 3) {
                item = ItemStack(Material.ARMOR_STAND)
                    .rename("§aAumentar o seu Clube em +1 slot para Membros")
                    .lore(
                        "§aPermita que o seu clube tenha mais pessoas! (Máximo: 20 membros)",
                        "§aLembre-se: Isto apenas afeta o seu clube atual, se você deletar",
                        "§ao seu clube, você irá perder os slots adicionais!",
                        "§f",
                        "§c50 pesadelos"
                    )
                    .meta<ItemMeta> {
                        setCustomModelData(1)
                    }

                onClick {
                    checkIfPlayerHasSufficientMoney(sender, 50) {
                        askForConfirmation(sender) {
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
                                        Cash.takeCash(sender, 50, TransactionContext(extra = "comprar `slots adicionais para o clube` no `/lojacash`"))
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
                        }
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

    fun askForConfirmation(sender: Player, afterAccept: () -> (Unit)) {
        val menu = createMenu(9, "§a§lConfirme a sua compra!") {
            slot(3, 0) {
                item = ItemStack(Material.GREEN_WOOL)
                    .rename("§a§lQuero comprar!")

                onClick {
                    afterAccept.invoke()
                }
            }
            slot(5, 0) {
                item = ItemStack(Material.RED_WOOL)
                    .rename("§c§lTalvez outro dia...")

                onClick {
                    sender.closeInventory()
                    showShopMenu(sender)
                }
            }
        }

        menu.sendTo(sender)
    }
}