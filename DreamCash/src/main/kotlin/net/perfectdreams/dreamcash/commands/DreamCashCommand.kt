package net.perfectdreams.dreamcash.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcash.DreamCash
import net.perfectdreams.dreamcash.dao.CashInfo
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamCashCommand(val m: DreamCash) : SparklyCommand(arrayOf("pesadelos", "dreamcash", "cash")) {
    @Subcommand
    fun root(sender: Player) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(sender.uniqueId)
            }

            val cash = cashInfo?.cash ?: 0

            switchContext(SynchronizationContext.SYNC)

            m.logger.info { "${sender.name} possui $cash pesadelos" }
            sender.sendMessage("${DreamCash.PREFIX} §eVocê tem §c${cash} pesadelos§e! Você pode comprar VIPs, sonhos e muito mais com pesadelos na §6/lojacash§e, dê uma passadinha lá!")
        }
    }

    @Subcommand
    fun checkPlayerCash(sender: CommandSender, name: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val cashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(DreamUtils.retrieveUserUniqueId(name))
            }

            val cash = cashInfo?.cash ?: 0

            switchContext(SynchronizationContext.SYNC)

            m.logger.info { "$name possui $cash pesadelos, verificado por ${sender.name}" }
            sender.sendMessage("${DreamCash.PREFIX} §b${name}§e tem §c$cash pesadelos§e!")
        }
    }

    @Subcommand(["pagar", "pay"])
    fun payPlayerCash(sender: Player, name: String, howMuchString: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val receiverInfo = transaction(Databases.databaseNetwork) {
                User.find { Users.username eq name }.firstOrNull()
            }

            if (receiverInfo == null) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §b${name} §cnão existe! A não ser se você está tentando dar pesadelos para o vento")
                return@schedule
            }

            if (sender.name.equals(name, true)) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQual é a graça de dar pesadelos para você mesmo? Você só vai continuar com a mesma quantidade!")
                return@schedule
            }

            var yourCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(sender.uniqueId)
            }

            var receiverCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(DreamUtils.retrieveUserUniqueId(name))
            }

            val howMuch = howMuchString.toIntOrNull()

            if (howMuch == null || 0 >= howMuch) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQuantidade de pesadelos inválida!")
                return@schedule
            }

            yourCashInfo = yourCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(sender.uniqueId) {
                    this.cash = 0
                }
            }

            if (howMuch > yourCashInfo.cash) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cVocê não tem tantos pesadelos!")
                return@schedule
            }

            receiverCashInfo = receiverCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(DreamUtils.retrieveUserUniqueId(name)) {
                    this.cash = 0
                }
            }

            transaction(Databases.databaseNetwork) {
                yourCashInfo.cash -= howMuch
                receiverCashInfo.cash += howMuch
            }

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §aProntinho! Você pagou §c${howMuch} pesadelos§a para §b${name}§a, agora você tem §c${yourCashInfo.cash} pesadelos, ${sender.name}§a!")
            val receivedPlayer = Bukkit.getPlayer(receiverCashInfo.id.value)
            receivedPlayer?.sendMessage("${DreamCash.PREFIX} §aVocê recebeu §c${howMuch} pesadelos§a de §b${sender.name}§a, agora você tem §c${receiverCashInfo.cash} pesadelos§a! Então, que tal comprar VIP? §6/lojacash")
        }
    }

    @Subcommand(["give"])
    @SubcommandPermission("dreamcash.give")
    fun givePlayerCash(sender: CommandSender, name: String, howMuchString: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            var receiverCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(DreamUtils.retrieveUserUniqueId(name))
            }

            val howMuch = howMuchString.toIntOrNull()

            if (howMuch == null || 0 >= howMuch) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQuantidade de pesadelos inválida!")
                return@schedule
            }

            receiverCashInfo = receiverCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(DreamUtils.retrieveUserUniqueId(name)) {
                    this.cash = 0
                }
            }

            val before = receiverCashInfo.cash

            transaction(Databases.databaseNetwork) {
                receiverCashInfo.cash += howMuch
            }

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §aProntinho! Você deu (tirando do além) §c${howMuch} pesadelos§a para §b${name}§a. Antes ele tinha §c$before pesadelos§a!")
        }
    }

    @Subcommand(["take"])
    @SubcommandPermission("dreamcash.take")
    fun takePlayerCash(sender: CommandSender, name: String, howMuchString: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            var receiverCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(DreamUtils.retrieveUserUniqueId(name))
            }

            val howMuch = howMuchString.toIntOrNull()

            if (howMuch == null || 0 >= howMuch) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQuantidade de pesadelos inválida!")
                return@schedule
            }

            receiverCashInfo = receiverCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(DreamUtils.retrieveUserUniqueId(name)) {
                    this.cash = 0
                }
            }

            val before = receiverCashInfo.cash

            transaction(Databases.databaseNetwork) {
                receiverCashInfo.cash -= howMuch
            }

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §aProntinho! Você tirou §c${howMuch} pesadelos§a de §b${name}§a. Antes ele tinha §c$before pesadelos§a!")
        }
    }

    @Subcommand(["set"])
    @SubcommandPermission("dreamcash.set")
    fun setPlayerCash(sender: CommandSender, name: String, howMuchString: String) {
        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            var receiverCashInfo = transaction(Databases.databaseNetwork) {
                CashInfo.findById(DreamUtils.retrieveUserUniqueId(name))
            }

            val howMuch = howMuchString.toIntOrNull()

            if (howMuch == null || 0 > howMuch) {
                switchContext(SynchronizationContext.SYNC)
                sender.sendMessage("${DreamCash.PREFIX} §cQuantidade de pesadelos inválida!")
                return@schedule
            }

            receiverCashInfo = receiverCashInfo ?: transaction(Databases.databaseNetwork) {
                CashInfo.new(DreamUtils.retrieveUserUniqueId(name)) {
                    this.cash = 0
                }
            }

            transaction(Databases.databaseNetwork) {
                receiverCashInfo.cash = howMuch.toLong()
            }

            switchContext(SynchronizationContext.SYNC)

            sender.sendMessage("${DreamCash.PREFIX} §aProntinho! Você setou §c${howMuch} pesadelos§a para §b${name}§a! Antes tinha §c${receiverCashInfo.cash} pesadelos§a!")
        }
    }
}