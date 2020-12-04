package net.perfectdreams.dreamterrainadditions.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.CommandException
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.isBetween
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object ConfigureClaimCommand : DSLCommandBase<DreamTerrainAdditions> {
    override fun command(plugin: DreamTerrainAdditions) = create(listOf("configurar")) {
        executes {
            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)

            if (claim == null) {
                player.sendMessage("§cFique em cima do terreno que você deseja configurar!")
                return@executes
            }

            if (claim.ownerName == player.name || claim.managers.contains(player.name)) {
                var _claimAdditions = plugin.getClaimAdditionsById(claim.id)

                if (_claimAdditions == null) {
                    _claimAdditions = DreamTerrainAdditions.ClaimAdditions(claim.id)
                    plugin.claimsAdditionsList.add(_claimAdditions)
                }

                val claimAdditions = _claimAdditions

                val humanizeBoolean: Boolean.() -> String = {
                    if (this) "§2habilitado§r§a" else "§cdesabilitado§r§a"
                }

                val menu = createMenu(9, "§aConfiguração do seu Terreno") {
                    slot(0, 0) {
                        item = ItemStack(Material.IRON_SWORD)
                            .apply {
                                if (claimAdditions.pvpEnabled)
                                    this.addUnsafeEnchantment(Enchantment.LUCK, 1)
                            }
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.pvpEnabled) {
                                    setDisplayName("§c§lDesativar PvP no Terreno")
                                } else {
                                    setDisplayName("§a§lAtivar PvP no Terreno")
                                }

                                lore = listOf(
                                    "§7Permita que players possam se atacar no seu terreno",
                                    "§7",
                                    "§7Players não irão perder itens ao morrer!"
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.pvpEnabled = !claimAdditions.pvpEnabled
                            it.sendMessage("§aPvP agora está ${humanizeBoolean(claimAdditions.pvpEnabled)} no seu terreno!")
                        }
                    }

                    slot(6, 0) {
                        item = ItemStack(Material.SNOWBALL)
                                .apply {
                                    if (claimAdditions.disableSnowFormation)
                                        this.addUnsafeEnchantment(Enchantment.LUCK, 1)
                                }
                                .meta<ItemMeta> {
                                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                    if (claimAdditions.disableSnowFormation) {
                                        setDisplayName("§c§lPermitir formação de neve no terreno")
                                    } else {
                                        setDisplayName("§a§lNão permitir formação de neve no terreno")
                                    }

                                    lore = listOf(
                                            "§7Bloqueie formação de neve no seu claim caso ele esteja em um bioma de neve"
                                    )
                                }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disableSnowFormation = !claimAdditions.disableSnowFormation

                            it.sendMessage("§aBloqueio de formação de neve agora está ${humanizeBoolean(claimAdditions.disableSnowFormation)} no seu terreno!")
                        }
                    }

                    slot(7, 0) {
                        item = ItemStack(Material.CREEPER_HEAD)
                            .apply {
                                if (claimAdditions.disableHostileMobs)
                                    this.addUnsafeEnchantment(Enchantment.LUCK, 1)
                            }
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disableHostileMobs) {
                                    setDisplayName("§c§lAtivar Spawn de Mobs Agressivos no Terreno")
                                } else {
                                    setDisplayName("§a§lDesativar Spawn de Mobs Agressivos no Terreno")
                                }

                                lore = listOf(
                                    "§7Bloqueie que mobs agressivos possam spawnar no sue terreno"
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disableHostileMobs = !claimAdditions.disableHostileMobs
                            it.sendMessage("§aBloqueio de mobs agressivos agora está ${humanizeBoolean(claimAdditions.disableHostileMobs)} no seu terreno!")
                        }
                    }

                    slot(8, 0) {
                        item = ItemStack(Material.PLAYER_HEAD)
                            .apply {
                                if (claimAdditions.disablePassiveMobs)
                                    this.addUnsafeEnchantment(Enchantment.LUCK, 1)
                            }
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disablePassiveMobs) {
                                    setDisplayName("§c§lAtivar Spawn de Mobs Passivos no Terreno")
                                } else {
                                    setDisplayName("§a§lDesativar Spawn de Mobs Passivos no Terreno")
                                }

                                lore = listOf(
                                    "§7Bloqueie que mobs passivos possam spawnar no sue terreno"
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disablePassiveMobs = !claimAdditions.disablePassiveMobs
                            it.sendMessage("§aBloqueio de mobs passivos agora está ${humanizeBoolean(claimAdditions.disablePassiveMobs)} no seu terreno!")
                        }
                    }
                }

                menu.sendTo(player)
            } else {
                player.sendMessage("§cEste terreno não é seu!")
            }
        }
    }
}