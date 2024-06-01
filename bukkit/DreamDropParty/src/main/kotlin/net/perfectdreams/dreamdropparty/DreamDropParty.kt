package net.perfectdreams.dreamdropparty

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamdropparty.commands.DropPartyCommand
import net.perfectdreams.dreamdropparty.events.DropParty
import net.perfectdreams.dreamdropparty.utils.LocationWrapper
import net.perfectdreams.dreamdropparty.utils.RandomItem
import net.perfectdreams.dreamjetpack.DreamJetpack
import org.bukkit.inventory.ItemStack
import java.io.File
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta


class DreamDropParty : KotlinPlugin() {
    companion object {
        const val FESTA_DA_FIRMA = "Festa da Firma"
        const val PREFIX = "§8[§x§0§3§f§c§f§c§lF§x§0§3§9§1§f§c§le§x§0§3§2§7§f§c§ls§x§4§a§0§3§f§c§lt§x§b§5§0§3§f§c§la §x§f§c§0§3§6§e§ld§x§f§c§0§3§0§3§la §x§f§c§d§8§0§3§lF§x§b§5§f§c§0§3§li§x§4§a§f§c§0§3§lr§x§0§3§f§c§2§7§lm§x§0§3§f§c§9§1§la§8]§e"
    }

    var prizes = mutableListOf<RandomItem>()

    lateinit var config: Config
    lateinit var dropParty: DropParty

    override fun softEnable() {
        super.softEnable()

        this.dataFolder.mkdirs()

        val file = File(this.dataFolder, "config.json")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(DreamUtils.gson.toJson(Config()))
        }

        config = DreamUtils.gson.fromJson(file.readText(), Config::class.java)
        dropParty = DropParty(this)

        registerCommand(DropPartyCommand(this))

        DreamCore.INSTANCE.dreamEventManager.events.add(dropParty)

        var chance = 0.1
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.BEACON
                ), chance
            )
        )
        chance = 0.2
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.NETHER_STAR
                ), chance
            )
        )
        chance = 0.5
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND_HELMET
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND_CHESTPLATE
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND_LEGGINGS
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND_BOOTS
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND_SWORD
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND_PICKAXE
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND_SHOVEL
                ), chance, true
            )
        )
        chance = 1.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_HELMET
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_CHESTPLATE
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_LEGGINGS
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_BOOTS
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_SWORD
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_PICKAXE
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_SHOVEL
                ), chance, true
            )
        )
        chance = 2.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_HELMET
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_CHESTPLATE
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_LEGGINGS
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_BOOTS
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_SWORD
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_PICKAXE
                ), chance, true
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_SHOVEL
                ), chance, true
            )
        )
        chance = 1.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.ANVIL
                ), chance
            )
        )
        chance = 1.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLDEN_APPLE,
                    1
                ), chance
            )
        )
        for (material in Material.values().filter { !it.isLegacy }) {
            if (material.name.startsWith("MUSIC_DISC_")) {
                prizes.add(
                    RandomItem(
                        ItemStack(
                            material,
                            1
                        ), chance
                    )
                )
            }

            if (material.name.endsWith("STAINED_GLASS")) {
                prizes.add(
                    RandomItem(
                        ItemStack(
                            material,
                            4
                        ), chance
                    )
                )
            }

            if (material.name.endsWith("STAINED_GLASS_PANE")) {
                prizes.add(
                    RandomItem(
                        ItemStack(
                            material,
                            8
                        ), chance
                    )
                )
            }

            if (material.name.endsWith("TERRACOTTA")) {
                prizes.add(
                    RandomItem(
                        ItemStack(
                            material,
                            8
                        ), chance
                    )
                )
            }
        }

        chance = 2.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.SLIME_BLOCK
                ), chance
            )
        )
        chance = 2.0
        prizes.add(
            RandomItem(
                ItemStack(Material.CHAINMAIL_CHESTPLATE)
                    .meta<ItemMeta> {
                        displayNameWithoutDecorations {
                            color(NamedTextColor.GOLD)
                            decorate(TextDecoration.BOLD)
                            content("Jetpack")
                        }

                        persistentDataContainer.set(DreamJetpack.IS_JETPACK_KEY, true)
                    }
                , chance
            )
        )
        chance = 3.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.HOPPER
                ), chance
            )
        )
        chance = 3.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.SPONGE,
                    2
                ), chance
            )
        )
        chance = 4.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.EMERALD
                ), chance
            )
        )
        chance = 4.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.QUARTZ_BLOCK,
                    16
                ), chance
            )
        )
        chance = 4.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.CHISELED_QUARTZ_BLOCK,
                    16
                ), chance
            )
        )
        chance = 4.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.QUARTZ_PILLAR,
                    16
                ), chance
            )
        )
        chance = 4.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.JUKEBOX
                ), chance
            )
        )
        chance = 4.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.NOTE_BLOCK
                ), chance
            )
        )
        chance = 4.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.EXPERIENCE_BOTTLE,
                    16
                ), chance
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.OAK_LOG,
                    64
                ), chance
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.ACACIA_LOG,
                    64
                ), chance
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.BIRCH_LOG,
                    64
                ), chance
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DARK_OAK_LOG,
                    64
                ), chance
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.SPRUCE_LOG,
                    64
                ), chance
            )
        )
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.JUNGLE_LOG,
                    64
                ), chance
            )
        )

        chance = 5.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.END_STONE,
                    16
                ), chance
            )
        )
        chance = 5.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.DIAMOND,
                    1
                ), chance
            )
        )
        chance = 6.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GOLD_INGOT,
                    16
                ), chance
            )
        )
        chance = 7.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.IRON_INGOT,
                    32
                ), chance
            )
        )
        chance = 7.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.NOTE_BLOCK,
                    4
                ), chance
            )
        )
        chance = 8.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.REDSTONE,
                    64
                ), chance
            )
        )
        chance = 12.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.CAKE
                ), chance
            )
        )
        chance = 12.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.GLOWSTONE,
                    12
                ), chance
            )
        )
        chance = 12.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.SEA_LANTERN,
                    4
                ), chance
            )
        )
        chance = 12.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.PRISMARINE,
                    4
                ), chance
            )
        )
        chance = 12.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.PRISMARINE,
                    4,
                    1.toShort()
                ), chance
            )
        )
        chance = 12.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.PRISMARINE,
                    4,
                    2.toShort()
                ), chance
            )
        )
        chance = 14.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.PAINTING,
                    8
                ), chance
            )
        )
        chance = 15.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.COBBLESTONE,
                    64
                ), chance
            )
        )
        chance = 16.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.COOKED_BEEF,
                    4
                ), chance
            )
        )
        chance = 16.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.COOKED_CHICKEN,
                    4
                ), chance
            )
        )
        chance = 16.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.COOKED_MUTTON,
                    4
                ), chance
            )
        )
        chance = 16.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.COOKED_RABBIT,
                    4
                ), chance
            )
        )
        chance = 16.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.COOKED_COD,
                    4
                ), chance
            )
        )
        chance = 16.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.BREAD,
                    4
                ), chance
            )
        )
        chance = 16.0
        prizes.add(
            RandomItem(
                ItemStack(
                    Material.APPLE,
                    4
                ), chance
            )
        )
    }

    override fun softDisable() {
        super.softDisable()

        DreamCore.INSTANCE.dreamEventManager.events.remove(dropParty)
    }

    class Config(var spawn: LocationWrapper = LocationWrapper("world", 0.0, 0.0, 0.0, 0f, 0f))
}