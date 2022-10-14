package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

class VitalsClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        withPlayerClube(player) { clube, selfMember ->
            val members = onAsyncThread { clube.retrieveMembers() }

            player.sendMessage("§8[ §bVitais §8]".centralizeHeader())
            player.sendMessage("§8Armas: §fM=Machado, E=Espada, A=Arco, F=Flecha".centralize())
            player.sendMessage("§8Materiais: §bdiamante§8, §eouro§8, §7pedra§8, §fferro§8, §6madeira§8".centralize())
            player.sendMessage("")
            val tg = TableGenerator(
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER
            )
            tg.addRow("§8Nome", "§8Vida§r", "§8Comida§r", "§8Armadura§r", "§8Armas")
            tg.addRow()

            for (wrapper in members) {
                val pStr = Bukkit.getPlayer(wrapper.id.value)
                if (pStr != null) {
                    var health = ""
                    var healthTimes = 0
                    while (20 > healthTimes) {
                        health = if (pStr.health >= healthTimes) {
                            "$health§a|"
                        } else {
                            "$health§4|"
                        }
                        ++healthTimes
                    }
                    var food = ""
                    var foodTimes = 0
                    while (20 > foodTimes) {
                        food = if (pStr.foodLevel >= foodTimes) {
                            "$food§a|"
                        } else {
                            "$food§4|"
                        }
                        ++foodTimes
                    }
                    var helmet = "§0C"
                    var chestplate = "§0P"
                    var leggings = "§0L"
                    var boots = "§0B"
                    var `is`: ItemStack? = null
                    `is` = pStr.inventory.helmet
                    if (`is` != null && `is`.getType().name.contains("HELMET")) {
                        if (`is`.getType().name.contains("DIAMOND")) {
                            helmet = "§bC"
                        }
                        if (`is`.getType().name.contains("GOLD")) {
                            helmet = "§eC"
                        }
                        if (`is`.getType().name.contains("IRON")) {
                            helmet = "§fC"
                        }
                        if (`is`.getType().name.contains("LEATHER")) {
                            helmet = "§6C"
                        }
                    }
                    `is` = pStr.inventory.chestplate
                    if (`is` != null) {
                        if (`is`.getType().name.contains("DIAMOND")) {
                            chestplate = "§bP"
                        }
                        if (`is`.getType().name.contains("GOLD")) {
                            chestplate = "§eP"
                        }
                        if (`is`.getType().name.contains("IRON")) {
                            chestplate = "§fP"
                        }
                        if (`is`.getType().name.contains("LEATHER")) {
                            chestplate = "§6P"
                        }
                    }
                    `is` = pStr.inventory.leggings
                    if (`is` != null) {
                        if (`is`.getType().name.contains("DIAMOND")) {
                            leggings = "§bL"
                        }
                        if (`is`.getType().name.contains("GOLD")) {
                            leggings = "§eL"
                        }
                        if (`is`.getType().name.contains("IRON")) {
                            leggings = "§fL"
                        }
                        if (`is`.getType().name.contains("LEATHER")) {
                            leggings = "§6L"
                        }
                    }
                    `is` = pStr.inventory.boots
                    if (`is` != null) {
                        if (`is`.getType().name.contains("DIAMOND")) {
                            boots = "§bB"
                        }
                        if (`is`.getType().name.contains("GOLD")) {
                            boots = "§eB"
                        }
                        if (`is`.getType().name.contains("IRON")) {
                            boots = "§fB"
                        }
                        if (`is`.getType().name.contains("LEATHER")) {
                            boots = "§6B"
                        }
                    }
                    var hasSword = false
                    var hasAxe = false
                    var hasBow = false
                    var hasArrow = false
                    for (item in pStr.inventory) {
                        if (item != null) {
                            if (item.type.name.contains("SWORD")) {
                                hasSword = true
                            } else if (item.type.name.contains("AXE")) {
                                hasAxe = true
                            } else if (item.type.name.contains("BOW")) {
                                hasBow = true
                            } else {
                                if (!item.type.name.contains("ARROW")) {
                                    continue
                                }
                                hasArrow = true
                            }
                        }
                    }
                    var weapons = ""
                    weapons = if (hasSword) {
                        "$weapons§fE"
                    } else {
                        "$weapons§0E"
                    }
                    weapons = if (hasAxe) {
                        "$weapons§fM"
                    } else {
                        "$weapons§0M"
                    }
                    weapons = if (hasBow) {
                        "$weapons§fA"
                    } else {
                        "$weapons§0A"
                    }
                    weapons = if (hasArrow) {
                        "$weapons§fF"
                    } else {
                        "$weapons§0F"
                    }
                    tg.addRow(
                        pStr.name,
                        health,
                        food,
                        helmet + chestplate + leggings + boots,
                        weapons
                    )
                }
            }

            for (line in tg.generate(TableGenerator.Receiver.CLIENT, true, true)) {
                player.sendMessage(line)
            }
            player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
        }
    }
}