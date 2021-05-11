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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction

class LojaCashCommand(val m: DreamCash) : SparklyCommand(arrayOf("lojacash", "cashloja")) {
    @Subcommand
    fun root(sender: Player) {
        showShopMenu(sender)
    }

    fun showShopMenu(sender: Player) {
        val menu = createMenu(45, "§a§lA Loja de seus §c§lPesadelos") {
            fun generateItemAt(x: Int, y: Int, type: Material, name: String, quantity: Long, callback: () -> (Unit)) {
                slot(x, y) {
                    item = ItemStack(type)
                        .rename(name)
                        .lore(
                            "§c$quantity pesadelos"
                        )

                    onClick {
                        checkIfPlayerHasSufficientMoney(sender, quantity) {
                            askForConfirmation(sender) {
                                sender.closeInventory()

                                scheduler().schedule(m, SynchronizationContext.ASYNC) {
                                    transaction(Databases.databaseNetwork) {
                                        try {
                                            Cash.takeCash(sender, quantity)
                                        } catch (e: IllegalArgumentException) {
                                            sender.sendMessage("§cVocê não tem pesadelos suficientes para comprar isto!")
                                            return@transaction
                                        }
                                    }

                                    switchContext(SynchronizationContext.SYNC)

                                    callback.invoke()
                                    sender.sendMessage("§aObrigado pela compra! ^-^")

                                    Bukkit.broadcastMessage("${DreamCash.PREFIX} §b${sender.displayName}§a comprou $name§a na loja de §cpesadelos§a (§6/lojacash§a), agradeça por ter ajudado a manter o §4§lSparkly§b§lPower§a online! ^-^")
                                }
                            }
                        }
                    }
                }
            }

            // VIPs
            generateItemAt(0, 0, Material.IRON_INGOT, "§b§lVIP §7(um mês • R$ 14,99)", 500) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user ${sender.name} parent addtemp vip 32d")
            }
            generateItemAt(1, 0, Material.GOLD_INGOT, "§b§lVIP§e+ §7(um mês • R$ 29,99)", 1000) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user ${sender.name} parent addtemp vip+ 32d")
            }
            generateItemAt(2, 0, Material.DIAMOND, "§b§lVIP§e++ §7(um mês • R$ 44,99)", 1500) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user ${sender.name} parent addtemp vip++ 32d")
            }

            // Blocos de Proteção
            generateItemAt(0, 1, Material.DIRT, "§e8000 blocos de proteção", 15) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 8000")
            }
            generateItemAt(1, 1, Material.MYCELIUM, "§e16000 blocos de proteção", 30) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 16000")
            }
            generateItemAt(2, 1, Material.GRASS_BLOCK, "§e24000 blocos de proteção", 45) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "adjustbonusclaimblocks ${sender.name} 24000")
            }

            // Money
            generateItemAt(0, 2, Material.EMERALD, "§a130000 Sonhos", 250) {
                sender.balance += 130000
            }

            generateItemAt(1, 2, Material.EMERALD, "§a260000 Sonhos", 500) {
                sender.balance += 260000
            }

            generateItemAt(2, 2, Material.EMERALD, "§a500000 Sonhos", 950) {
                sender.balance += 500000
            }

            generateItemAt(3, 2, Material.EMERALD, "§a1000000 Sonhos", 1_900) {
                sender.balance += 1000000
            }

            generateItemAt(4, 2, Material.EMERALD, "§a2000000 Sonhos", 3_800) {
                sender.balance += 2000000
            }

            generateItemAt(5, 2, Material.EMERALD, "§a5000000 Sonhos", 9_500) {
                sender.balance += 5000000
            }

            generateItemAt(6, 2, Material.EMERALD, "§a10000000 Sonhos", 19_000) {
                sender.balance += 10000000
            }

            // Coisas Reais
            this.slot(0, 3) {
                item = ItemStack(Material.OAK_BOAT)
                    .rename("§aAumentar o seu Clube em +1 slot para Membros")
                    .lore(
                        "§aPermita que o seu clube tenha mais pessoas! (Máximo: 20 membros)",
                        "§aLembre-se: Isto apenas afeta o seu clube atual, se você deletar",
                        "§ao seu clube, você irá perder os slots adicionais!",
                        "§f",
                        "§c50 pesadelos"
                    )

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
                                        Cash.takeCash(sender, 50)
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
            this.slot(7, 4) {
                item = ItemStack(Material.NETHER_STAR)
                    .rename("§aComo conseguir pesadelos?")

                onClick {
                    it.closeInventory()

                    sender.sendMessage("§aExistem vários jeitos de conseguir §cpesadelos§a!")
                    sender.sendMessage("§8• §eNa nossa loja§b https://sparklypower.net/loja")
                    sender.sendMessage("§8• §eVotando no servidor §6/votar")
                    sender.sendMessage("§8• §eVencendo eventos no servidor")
                }
            }

            this.slot(8, 4) {
                item = ItemStack(Material.BARRIER)
                    .rename("§c§lFechar menu")

                onClick {
                    it.closeInventory()
                }
            }

            this.slot(6,  4) {
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