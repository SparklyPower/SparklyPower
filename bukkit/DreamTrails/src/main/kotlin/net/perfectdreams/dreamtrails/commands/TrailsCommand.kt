package net.perfectdreams.dreamtrails.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamtrails.DreamTrails
import net.perfectdreams.dreamtrails.trails.TrailData
import net.perfectdreams.dreamtrails.utils.Halo
import net.perfectdreams.dreamtrails.utils.PlayerTrailsData
import net.perfectdreams.dreamtrails.utils.Trails
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class TrailsCommand(val m: DreamTrails) : SparklyCommand(arrayOf("trails", "trail", "rastro", "rastros"), permission = DreamTrails.USE_TRAILS_PERMISSION) {
    @Subcommand
    fun trails(player: Player) {
        openChooseTrailMenu(player)
    }

    private fun openChooseTrailMenu(player: Player) {
        val chooseTrailMenu = createMenu(9, "§aEscolha o Tipo de Rastro!") {
            this.slot(3, 0) {
                item = ItemStack(Material.IRON_BLOCK)
                    .rename("§6§lRastros ao Andar")
                    .lore("§aPartículas que aparecem ao se mover")

                onClick {
                    it.closeInventory()

                    openMoveTrailMenu(player)
                }
            }

            this.slot(4, 0) {
                item = ItemStack(Material.LEATHER_BOOTS)
                    .rename("§d§lFashion")
                    .lore(
                        "§dNão é um rastro, é uma armadura que muda de cor automaticamente,",
                        "§dpara mostrar como você é estiloso para as inimiga, ou para",
                        "§dmostrar seu apoio ao movimento LGBT."
                    )

                onClick {
                    it.closeInventory()

                    if (!player.hasPermission(DreamTrails.USE_FASHION_ARMOR_PERMISSION)) {
                        player.sendMessage("§cVocê precisa ser §b§lVIP§e+§c para poder ser fashion amigxxx!")
                        return@onClick
                    }

                    player.inventory
                        .addItem(
                            ItemStack(Material.LEATHER_HELMET)
                                .rename("§d§lChapéuzinho Fashion")
                                .lore(
                                    "§7Seja §b§lVIP§e+§7, coloque no corpo e seja fashion amigxxx!"
                                )
                                .meta<ItemMeta> {
                                    persistentDataContainer.set(DreamTrails.IS_FANCY_LEATHER_ARMOR_KEY, true)
                                }
                        )

                    player.inventory
                        .addItem(
                            ItemStack(Material.LEATHER_CHESTPLATE)
                                .rename("§d§lCamiseta Fashion")
                                .lore(
                                    "§7Seja §b§lVIP§e+§7, coloque no corpo e seja fashion amigxxx!"
                                )
                                .meta<ItemMeta> {
                                    persistentDataContainer.set(DreamTrails.IS_FANCY_LEATHER_ARMOR_KEY, true)
                                }
                        )

                    player.inventory
                        .addItem(
                            ItemStack(Material.LEATHER_LEGGINGS)
                                .rename("§d§Calça Fashion")
                                .lore(
                                    "§7Seja §b§lVIP§e+§7, coloque nas calças e seja fashion amigxxx!"
                                )
                                .meta<ItemMeta> {
                                    persistentDataContainer.set(DreamTrails.IS_FANCY_LEATHER_ARMOR_KEY, true)
                                }
                        )

                    player.inventory
                        .addItem(
                            ItemStack(Material.LEATHER_BOOTS)
                                .rename("§d§lBotas Fashion")
                                .lore(
                                    "§7Seja §b§lVIP§e+§7, coloque no pé e seja fashion amigxxx!"
                                )
                                .meta<ItemMeta> {
                                    persistentDataContainer.set(DreamTrails.IS_FANCY_LEATHER_ARMOR_KEY, true)
                                }
                        )

                    player.sendMessage("§aEu dei a armadura fashion, agora é só equipar e se divertir! ^-^")
                }
            }

            this.slot(5, 0) {
                item = ItemStack(Material.DIAMOND_BLOCK)
                    .rename("§e§lAuréolas de Anjo")
                    .lore("§aPartículas que aparecem em cima da sua cabeça")

                onClick {
                    it.closeInventory()

                    if (!player.hasPermission(DreamTrails.USE_HALO_PERMISSION)) {
                        player.sendMessage("§cVocê precisa ser §b§lVIP§e++§c para poder ser considerado um anjinho!")
                        return@onClick
                    }

                    openHaloMenu(it as Player)
                }
            }
        }

        chooseTrailMenu.sendTo(player)
    }

    private fun openMoveTrailMenu(player: Player) {
        val trailMenu = createMenu(54, "§aConfigure o seu Rastro") {
            for ((index, trail) in Trails.trails.values.withIndex()) {
                this.slot(index % 9, index / 9) {
                    item = ItemStack(trail.material)
                        .rename(trail.name)

                    onMoveTrailClick(player, trail)
                }
            }

            this.slot(8, 5) {
                item = ItemStack(Material.BARRIER)
                    .rename("§c§lRemover Auréola")

                onClick {
                    player.closeInventory()

                    val playerTrailsData = m.playerTrails.getOrPut(player.uniqueId, { PlayerTrailsData() })

                    playerTrailsData.activeParticles.clear()
                    player.sendMessage("§aTodos os efeitos foram removidos!")
                }
            }
        }

        trailMenu.sendTo(player)
    }

    private fun DreamMenuSlotBuilder.onMoveTrailClick(player: Player, trail: TrailData) {
        this.onClick {
            player.closeInventory()

            val playerTrailsData = m.playerTrails.getOrPut(player.uniqueId, { PlayerTrailsData() })

            if (playerTrailsData.activeParticles.contains(trail.particle)) {
                playerTrailsData.activeParticles.remove(trail.particle)
                player.sendMessage("§aRastro desativado!")
            } else {
                if (playerTrailsData.activeParticles.isNotEmpty() && !player.hasPermission("dreamtrails.stack")) {
                    player.sendMessage("§cApenas §b§lVIPs§e++§c podem empilhar partículas, desative outras partículas antes de ativar outra!")
                    return@onClick
                }
                if (playerTrailsData.activeParticles.size == 7) {
                    player.sendMessage("§cVocê já tem muitas partículas empilhadas!")
                    return@onClick
                }

                playerTrailsData.activeParticles.add(trail.particle)
                player.sendMessage("§aRastro ativado!")
            }
        }
    }

    private fun openHaloMenu(player: Player) {
        val haloMenu = createMenu(54, "§aConfigure a sua Auréola") {
            for ((index, halo) in Halo.values().withIndex()) {
                this.slot(index % 9, index / 9) {
                    item = ItemStack(halo.material)
                        .rename(halo.title)

                    onHaloClick(player, halo)
                }
            }

            this.slot(8, 5) {
                item = ItemStack(Material.BARRIER)
                    .rename("§c§lRemover Auréola")

                onClick {
                    player.closeInventory()

                    val playerTrailsData = m.playerTrails.getOrPut(player.uniqueId, { PlayerTrailsData() })

                    playerTrailsData.activeHalo = null
                    player.sendMessage("§aAuréola removida!")
                }
            }
        }

        haloMenu.sendTo(player)
    }

    private fun DreamMenuSlotBuilder.onHaloClick(player: Player, halo: Halo) {
        this.onClick {
            player.closeInventory()

            val playerTrailsData = m.playerTrails.getOrPut(player.uniqueId, { PlayerTrailsData() })

            playerTrailsData.activeHalo = halo
            player.sendMessage("§aVocê foi abençoado e agora virou um anjo oficial, aprecie a sua nova auréola!")
        }
    }

    private fun beautifyEnum(particle: Particle) = particle.name.split("_").map { it.toLowerCase().capitalize() }.joinToString(" ")
    private fun beautifyPermission(particle: Particle) = "dreamtrails.trail.${particle.name.replace("_", "")}"
}