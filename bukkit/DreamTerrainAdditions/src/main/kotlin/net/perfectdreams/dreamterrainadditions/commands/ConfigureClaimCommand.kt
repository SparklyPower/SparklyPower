package net.perfectdreams.dreamterrainadditions.commands

import com.destroystokyo.paper.profile.ProfileProperty
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

object ConfigureClaimCommand : DSLCommandBase<DreamTerrainAdditions> {
    override fun command(plugin: DreamTerrainAdditions) = create(listOf("configurar")) {
        executes {
            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)

            if (claim == null) {
                player.sendMessage("§cNenhnum terreno encontrado aqui, fique em cima do terreno que você deseja configurar!")
                return@executes
            }

            if (claim.ownerName == player.name || claim.allowGrantPermission(player) == null) {
                val claimAdditions = plugin.getOrCreateClaimAdditionsWithId(claim.id)

                val menu = createMenu(45, "§cConfiguração do terreno") {

                    slot(1, 1) {

                        item = ItemStack(Material.TNT)
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claim.areExplosivesAllowed) {
                                    setDisplayName("§cDesativar explosões no terreno")
                                } else {
                                    setDisplayName("§aAtivar explosões no terreno")
                                }

                                lore = listOf(
                                        "§7Permita ou bloqueie explosões em seu terreno"
                                )
                            }

                        onClick {
                            it.closeInventory()

                            claim.areExplosivesAllowed = !claim.areExplosivesAllowed

                            it.sendMessage("§aBloqueio de explosões em seu terreno agora está ${humanizeBoolean(claim.areExplosivesAllowed)}§a! §a" +
                                    humanizeBoolean(claim.areExplosivesAllowed,
                                            "Explosões agora podem danificar seu terreno.",
                                            "Explosões não irão danificar seu terreno."))
                        }
                    }

                    slot(3, 1) {

                        item = ItemStack(Material.SPRUCE_TRAPDOOR)
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disableTrapdoorAndDoorAccess) {
                                    setDisplayName("§c§lPermitir acesso a alçapões e portas")
                                } else {
                                    setDisplayName("§a§lBloquear acesso a alçapões e portas")
                                }

                                lore = listOf(
                                        "§7Bloqueie ou permita que outros jogadores possam",
                                        "§7abrir ou fechar portas e alçapões em seu claim"
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disableTrapdoorAndDoorAccess = !claimAdditions.disableTrapdoorAndDoorAccess

                            it.sendMessage("§aBloqueio de acesso à portas e alçapões para players sem permissão agora está ${humanizeBoolean(claimAdditions.disableTrapdoorAndDoorAccess)}§a! §a" +
                                    humanizeBoolean(claimAdditions.disableTrapdoorAndDoorAccess,
                                            "Players sem trust não irão conseguir abrir ou fechar portas e alçapões.",
                                            "Players sem trust conseguirão abrir ou fechar portas e alçapões."))
                        }
                    }

                    slot(5, 1) {

                        item = ItemStack(Material.SNOWBALL)
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disableSnowFormation) {
                                    setDisplayName("§c§lPermitir formação de neve no terreno")
                                } else {
                                    setDisplayName("§a§lNão permitir formação de neve no terreno")
                                }

                                lore = listOf(
                                        "§7Bloqueie formação de neve no seu",
                                        "§7claim caso ele esteja em um bioma de neve"
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disableSnowFormation = !claimAdditions.disableSnowFormation

                            it.sendMessage("§aBloqueio de formação de neve agora está ${humanizeBoolean(claimAdditions.disableSnowFormation)} no seu terreno!")
                        }
                    }

                    slot(7, 1) {

                        item = ItemStack(Material.PLAYER_HEAD)
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disablePassiveMobs) {
                                    setDisplayName("§c§lAtivar Spawn de Mobs Passivos no Terreno")
                                } else {
                                    setDisplayName("§a§lDesativar Spawn de Mobs Passivos no Terreno")
                                }

                                lore = listOf(
                                        "§7Bloqueie que mobs passivos possam spawnar no seu terreno"
                                )
                            }
                            .meta<SkullMeta> {
                                playerProfile = Bukkit.createProfile(UUID.fromString("0c86f354-712c-4142-a3af-8ad3f53e1d96")).apply {
                                    this.setProperty(
                                            ProfileProperty(
                                                    "textures",
                                                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzVhOWNkNThkNGM2N2JjY2M4ZmIxZjVmNzU2YTJkMzgxYzlmZmFjMjkyNGI3ZjRjYjcxYWE5ZmExM2ZiNWMifX19"
                                            )
                                    )
                                }
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disablePassiveMobs = !claimAdditions.disablePassiveMobs
                            it.sendMessage("§aBloqueio de mobs passivos agora está ${humanizeBoolean(claimAdditions.disablePassiveMobs)} §ano seu terreno! §a" +
                                    humanizeBoolean(claimAdditions.disablePassiveMobs,
                                            "Mobs que não atacam irão parar de spawnar em seu terreno.",
                                            "Mobs que não atacam, agora, irão spawnar normalmente em seu terreno."))
                        }
                    }

                    slot(2, 2) {

                        item = ItemStack(Material.CREEPER_HEAD)
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disableHostileMobs) {
                                    setDisplayName("§c§lAtivar Spawn de Mobs Agressivos no Terreno")
                                } else {
                                    setDisplayName("§a§lDesativar Spawn de Mobs Agressivos no Terreno")
                                }

                                lore = listOf(
                                        "§7Bloqueie que mobs agressivos possam spawnar no seu terreno"
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disableHostileMobs = !claimAdditions.disableHostileMobs
                            it.sendMessage("§aBloqueio de mobs agressivos agora está ${humanizeBoolean(claimAdditions.disableHostileMobs)} no seu terreno! §a" +
                                    humanizeBoolean(claimAdditions.disableHostileMobs,
                                            "Mobs que atacam irão parar de spawnar em seu terreno.",
                                            "Mobs que atacam irão voltar a spawnar em seu terreno."))
                        }
                    }

                    slot(4, 2) {

                        item = ItemStack(Material.IRON_SWORD)
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

                    slot(6, 2) {

                        item = ItemStack(Material.BARRIER)
                                .meta<ItemMeta> {
                                    this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                    setDisplayName("§cVer lista de players banidos do terreno")

                                    lore = listOf(
                                            "§7Veja os players que estão banidos deste claim",
                                            "§7",
                                            "§7Utilize §6/banir <nome>",
                                            "§7para banir alguém do seu terreno"
                                    )
                                }

                        onClick {
                            it.closeInventory()
                            player.performCommand("claimbanlist")
                        }
                    }
                    slot(1, 3) {
                        item = ItemStack(Material.WHEAT_SEEDS)
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disableCropGrowth) {
                                    setDisplayName("§a§lPermitir o crescimento de sementes e plantações no terreno")
                                } else {
                                    setDisplayName("§b§lImpedir o crescimento de sementes e plantações no terreno")
                                }

                                lore = listOf(
                                    "§7Bloqueie o crescimento de qualquer tipo de sementes em seu terreno.",
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disableCropGrowth = !claimAdditions.disableCropGrowth
                            it.sendMessage("§aO bloqueio de crescimento de plantações está agora ${humanizeBoolean(claimAdditions.disableCropGrowth)} no seu terreno!")
                        }
                    }
                    slot(3, 3) {
                        item = ItemStack(Material.VINE)
                            .meta<ItemMeta> {
                                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                if (claimAdditions.disablePlantsSpreading) {
                                    setDisplayName("§a§lPermitir o espalhamento de plantas no terreno")
                                } else {
                                    setDisplayName("§b§lImpedir o espalhamento de plantas no terreno")
                                }

                                lore = listOf(
                                    "§7Bloqueie o espalhamento de qualquer tipo de plantas em seu terreno, como vinhas.",
                                )
                            }

                        onClick {
                            it.closeInventory()
                            claimAdditions.disablePlantsSpreading = !claimAdditions.disablePlantsSpreading
                            it.sendMessage("§aO bloqueio de crescimento de plantas está agora ${humanizeBoolean(claimAdditions.disablePlantsSpreading)} no seu terreno!")
                        }
                    }
                }

                menu.sendTo(player)
            } else {
                player.sendMessage("§cVocê não possui permissão para configurar este claim! Peça para que o dono do claim te dê permissão para configurar utilizando o comando §6/permissiontrust§c.")
            }
        }
    }

    fun humanizeBoolean(bool: Boolean, whenEnabled: String = "§2habilitado§a", whenDisabled: String = "§cdesabilitado§a"): String {

        return if (bool) whenEnabled else whenDisabled
    }
}