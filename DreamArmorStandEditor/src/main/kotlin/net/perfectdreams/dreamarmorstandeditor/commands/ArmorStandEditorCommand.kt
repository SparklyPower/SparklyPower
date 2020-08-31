package net.perfectdreams.dreamarmorstandeditor.commands

import net.perfectdreams.dreamarmorstandeditor.DreamArmorStandEditor
import net.perfectdreams.dreamarmorstandeditor.utils.ArmorStandEditorType
import net.perfectdreams.dreamarmorstandeditor.utils.EditType
import net.perfectdreams.dreamcore.utils.commands.CommandException
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.canPlaceAt
import net.perfectdreams.dreamcore.utils.rename
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ArmorStandEditorCommand : DSLCommandBase<DreamArmorStandEditor> {
    override fun command(plugin: DreamArmorStandEditor) = create(
        listOf("armorstandeditor")
    ) {
        permission = "dreamarmorstandeditor.setup"

        executes {
            val entity = player.getTargetEntity(15)

            if (entity?.type == EntityType.ARMOR_STAND) {
                plugin.isRotating.remove(player)

                if (!player.canPlaceAt(entity.location, Material.ARMOR_STAND))
                    throw CommandException("§cVocê não tem permissão para editar esta armor stand!")

                val armorStand = entity as ArmorStand

                val menu = createMenu(36, "§5§lEditor de Armor Stands") {
                    slot(0, 0) {
                        item = ItemStack(Material.TOTEM_OF_UNDYING)
                            .rename("§a§lAlterar tamanho")

                        onClick {
                            it.closeInventory()

                            armorStand.isSmall = !armorStand.isSmall
                        }
                    }

                    slot(1, 0) {
                        item = ItemStack(Material.DIAMOND_SWORD)
                            .rename("§a§lMostrar/Esconder braços")

                        onClick {
                            it.closeInventory()

                            armorStand.setArms(!armorStand.hasArms())
                        }
                    }

                    slot(2, 0) {
                        item = ItemStack(Material.SMOOTH_STONE_SLAB)
                            .rename("§a§lMostrar/Esconder base")

                        onClick {
                            it.closeInventory()

                            armorStand.setBasePlate(!armorStand.hasBasePlate())
                        }
                    }

                    slot(7, 0) {
                        item = ItemStack(Material.PLAYER_HEAD)
                            .rename("§a§lAlterar posição da cabeça")

                        onClick {
                            it.closeInventory()

                            openTypeSubMenu(player, plugin, ArmorStandEditorType.HEAD_PITCH, ArmorStandEditorType.HEAD_YAW, ArmorStandEditorType.HEAD_ROLL, armorStand)
                        }
                    }

                    slot(6, 1) {
                        item = ItemStack(Material.STICK)
                            .rename("§a§lAlterar posição do braço esquerdo")

                        onClick {
                            it.closeInventory()

                            openTypeSubMenu(player, plugin, ArmorStandEditorType.LEFT_ARM_PITCH, ArmorStandEditorType.LEFT_ARM_YAW, ArmorStandEditorType.LEFT_ARM_ROLL, armorStand)
                        }
                    }

                    slot(7, 1) {
                        item = ItemStack(Material.DIAMOND_CHESTPLATE)
                            .rename("§a§lAlterar posição do corpo")

                        onClick {
                            it.closeInventory()

                            openTypeSubMenu(player, plugin, ArmorStandEditorType.BODY_PITCH, ArmorStandEditorType.BODY_YAW, ArmorStandEditorType.BODY_ROLL, armorStand)
                        }
                    }

                    slot(8, 1) {
                        item = ItemStack(Material.STICK)
                            .rename("§a§lAlterar posição do braço direito")

                        onClick {
                            it.closeInventory()

                            openTypeSubMenu(player, plugin, ArmorStandEditorType.RIGHT_ARM_PITCH, ArmorStandEditorType.RIGHT_ARM_YAW, ArmorStandEditorType.RIGHT_ARM_ROLL, armorStand)
                        }
                    }

                    slot(6, 2) {
                        item = ItemStack(Material.TWISTING_VINES)
                            .rename("§a§lAlterar posição da perna esquerda")

                        onClick {
                            it.closeInventory()

                            openTypeSubMenu(player, plugin, ArmorStandEditorType.LEFT_LEG_PITCH, ArmorStandEditorType.LEFT_LEG_YAW, ArmorStandEditorType.LEFT_LEG_ROLL, armorStand)
                        }
                    }

                    slot(8, 2) {
                        item = ItemStack(Material.TWISTING_VINES)
                            .rename("§a§lAlterar posição da perna direita")

                        onClick {
                            it.closeInventory()

                            openTypeSubMenu(player, plugin, ArmorStandEditorType.RIGHT_LEG_PITCH, ArmorStandEditorType.RIGHT_LEG_YAW, ArmorStandEditorType.RIGHT_LEG_ROLL, armorStand)
                        }
                    }
                }

                menu.sendTo(player)
            } else {
                throw CommandException("§cOlhe para a armor stand que você deseja editar!")
            }
        }
    }

    fun openTypeSubMenu(
        player: Player,
        plugin: DreamArmorStandEditor,
        pitch: ArmorStandEditorType,
        yaw: ArmorStandEditorType,
        roll: ArmorStandEditorType,
        armorStand: ArmorStand
    ) {
        val menu = createMenu(9, "§5§lEscolha o movimento") {
            slot(3, 0) {
                item = ItemStack(Material.DIAMOND)
                    .rename("§a§lAlterar inclinação")

                onClick {
                    it.closeInventory()

                    plugin.isRotating[player] = EditType(armorStand, pitch)

                    player.sendMessage("§aPara alterar a inclinação, clique com botão direito na armor stand! Caso queira diminuir a intensidade da inclinação, segure SHIFT ao clicar!")
                }
            }

            slot(4, 0) {
                item = ItemStack(Material.DIAMOND)
                    .rename("§a§lAlterar rotação")

                onClick {
                    it.closeInventory()

                    plugin.isRotating[player] = EditType(armorStand, yaw)

                    player.sendMessage("§aPara alterar a rotação, clique com botão direito na armor stand! Caso queira diminuir a intensidade da rotação, segure SHIFT ao clicar!")
                }
            }

            slot(5, 0) {
                item = ItemStack(Material.DIAMOND)
                    .rename("§a§lAlterar giro")

                onClick {
                    it.closeInventory()

                    plugin.isRotating[player] = EditType(armorStand, roll)

                    player.sendMessage("§aPara alterar o giro, clique com botão direito na armor stand! Caso queira diminuir a intensidade do giro, segure SHIFT ao clicar!")
                }
            }
        }

        menu.sendTo(player)
    }
}