package net.perfectdreams.dreammoverspawners

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.CreatureSpawner
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class DreamMoverSpawners : KotlinPlugin(), Listener {
    val onlyInWorld = listOf("world")
    val onlyInNether = listOf("nether")
    val validSpawners = listOf(
        MobSpawnerBreakable(
            EntityType.ZOMBIE,
            onlyInWorld
        ),
        MobSpawnerBreakable(
            EntityType.CAVE_SPIDER,
            onlyInWorld
        ),
        MobSpawnerBreakable(
            EntityType.SPIDER,
            onlyInWorld
        ),
        MobSpawnerBreakable(
            EntityType.CREEPER,
            onlyInWorld
        ),
        MobSpawnerBreakable(
            EntityType.SKELETON,
            onlyInWorld
        ),
        MobSpawnerBreakable(
            EntityType.PIG_ZOMBIE,
            onlyInNether
        ),
        MobSpawnerBreakable(
            EntityType.BLAZE,
            onlyInNether
        )
    )

    override fun softEnable() {
        super.softEnable()

        registerCommand(object: SparklyCommand(arrayOf("moverspawners"), permission = "sparklypower.moverspawners") {
            @Subcommand
            fun spawn(player: Player) {
                val eachCount = player.world.entities.groupingBy { it.type }.eachCount()
                eachCount.entries.sortedByDescending { it.value }.first().key
                player.inventory.addItem(
                    ItemStack(Material.GOLDEN_PICKAXE)
                        .rename("§6§lPicareta de Mover Spawners")
                        .lore("§7Querendo mover spawners para outros lugares?", "§7Então utilize a incrível picareta de mover spawners!", "§7", "§7Cuidado que ela quebra bem rápido!")
                        .apply {
                            this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                            this.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
                        }
                        .storeMetadata("isMoveSpawners", "true")
                )
            }
        })

        registerEvents(this)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlace(e: BlockPlaceEvent) {
        if (e.itemInHand.type != Material.SPAWNER)
            return

        val center = e.block.location.add(0.5, 0.5, 0.5)
        val spawnerType = e.itemInHand.getStoredMetadata("spawnerType") ?: return
        val mobType = EntityType.valueOf(spawnerType)
        val spawnerX = validSpawners.firstOrNull { it.type == mobType }
        if (spawnerX == null) {
            e.isCancelled = true
            e.player.sendMessage("§cDesculpe, mas o meu poder não permite você colocar esses tipos de spawners...")
            center.world.spawnParticle(Particle.SPELL_WITCH, center, 1)
            return
        }

        if (e.block.world.name !in spawnerX.allowedWorlds) {
            e.isCancelled = true
            e.player.sendMessage("§cDesculpe, você não pode colocar esse spawner neste mundo...")
            center.world.spawnParticle(Particle.SPELL_WITCH, center, 1)
            return
        }

        val spawner = e.blockPlaced.state as CreatureSpawner
        spawner.spawnedType = mobType
        spawner.update()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        val inHand = e.player.inventory.itemInMainHand

        if (e.player.inventory.itemInMainHand?.type != Material.GOLDEN_PICKAXE)
            return

        if (inHand.getStoredMetadata("isMoveSpawners") != "true")
            return

        if (e.block.world.name == "MinaRecheada")
            return

        for (enchantment in inHand.enchantments.filter { it.key != Enchantment.ARROW_INFINITE })
            inHand.removeEnchantment(enchantment.key)

        val broken = e.block

        if (broken.type == Material.SPAWNER) {
            val mobSpawner = broken.state as CreatureSpawner
            val type = mobSpawner.spawnedType

            e.isCancelled = true

            val center = e.block.location.add(0.5, 0.5, 0.5)

            val spawner = validSpawners.firstOrNull { it.type == type }
            if (spawner == null) {
                e.player.sendMessage("§cDesculpe, mas o meu poder não permite quebrar esses tipos de spawners...")
                center.world.spawnParticle(Particle.SPELL_WITCH, center, 1)
                return
            }

            var newDurability = e.player.inventory.itemInMainHand.durability

            newDurability = when {
                e.player.hasPermission("dreammoverspawners.vip++") -> {
                    (newDurability + 1).toShort()
                }
                e.player.hasPermission("dreammoverspawners.vip+") -> {
                    (newDurability + 2).toShort()
                }
                else -> {
                    (newDurability + 4).toShort()
                }
            }

            e.player.inventory.itemInMainHand.durability = newDurability

            if (newDurability >= 32.toShort()) {
                e.player.inventory.itemInMainHand = ItemStack(Material.AIR)
                e.player.sendMessage("§cSua picareta de mover spawners quebrou!")
            }

            e.block.type = Material.AIR // rip
            e.block.world.dropItemNaturally(
                center,
                ItemStack(
                    Material.SPAWNER
                ).lore("§7Spawner de §a${type.getLocalizedName()}")
                    .storeMetadata("spawnerType", type.toString())
            )

            center.world.spawnParticle(Particle.VILLAGER_HAPPY, center, 8, 1.0, 1.0, 1.0)
            center.world.spawnParticle(Particle.FIREWORKS_SPARK, center, 8, 1.0, 1.0, 1.0)
            e.player.playSound(center, Sound.BLOCK_ANVIL_LAND, 1f, 1f)
        } else {
            e.isCancelled = true

            e.player.sendMessage("§cVocê está tentando quebrar um bloco normal com a picareta de mover spawners! Não faça isso, economize a durabilidade dela <3")
        }
    }

    data class MobSpawnerBreakable(
        val type: EntityType,
        val allowedWorlds: List<String>
    )
}