package net.perfectdreams.dreampicaretamonstra.listeners

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.minecraft.world.entity.item.ItemEntity
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.GeometryUtils
import net.perfectdreams.dreamcore.utils.PlayerUtils
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcustomitems.listeners.canMineRubyFrom
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreampicaretamonstra.DreamPicaretaMonstra
import org.bukkit.*
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftItem
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

class MonstraBlockListener(val m: DreamPicaretaMonstra) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        val inHand = e.player.inventory.itemInMainHand

        if (inHand.type != Material.DIAMOND_PICKAXE && inHand.type != Material.DIAMOND_SHOVEL) {
            return
        }

        if (inHand.getStoredMetadata("isMonsterPickaxe") != "true")
            return

        val damageable = inHand.itemMeta as Damageable
        m.logger.info("Player ${e.player.name} used a Picareta Monstra at ${e.player.world.name} ${e.block.location.x}, ${e.block.location.y}, ${e.block.location.z}. Damage value: ${damageable.damage}")

        val isInClaim = GriefPrevention.instance.dataStore.getClaimAt(e.block.location, false, null)
        val isInMinaRecheada = e.block.world.name == "MinaRecheada"

        if ((e.block.world.name == "world" && isInClaim == null) || isInMinaRecheada) {
            if (!isInMinaRecheada) e.player.sendMessage("§cVocê só pode usar a picareta monstra no seu terreno! Se você quer sair quebrando tudo, proteja o terreno ou vá no mundo de recursos, §6/warp recursos")
            return
        }

        val broken = e.block
        val heldItemType = inHand.type

        val isPicaretaMonstra = heldItemType.name.contains("_PICKAXE")

        if (inHand.hasItemMeta() && !inHand.itemMeta.hasCustomModelData()) {
            damageable.setCustomModelData(1)
        }

        if (m.isValidForHeldItem(heldItemType, broken)) {
            e.isCancelled = true
            val allDrops = mutableListOf<ItemStack>()

            val blocks = GeometryUtils.sphere(e.block.location, 2, false) as Set<Location>
            var enchantmentLevel = 0
            var efficiencyLevel = 0
            var isSilky = false
            enchantmentLevel = inHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
            efficiencyLevel = inHand.getEnchantmentLevel(Enchantment.DURABILITY)
            isSilky = inHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0
            var below = false
            if (e.player.location.y > e.block.y) {
                below = true
            }
            var shouldPlay = true

            for (location in blocks) {
                if ((below || location.blockY > e.player.location.y - 1.0) && location.block.type !== Material.AIR && location.block.type !== Material.BEDROCK && m.isValidForHeldItem(heldItemType, location.block)) {
                    val center = location.add(0.0, 0.5, 0.0)
                    val claim = GriefPrevention.instance.dataStore
                            .getClaimAt(location, false, null)

                    if (PlayerUtils.canBreakAt(location, e.player, location.block.type) && (e.player.world.name != "world" || (e.player.world.name == "world" && claim != null))) {
                        if (chance(100.0 / (efficiencyLevel + 1))) {
                            if ((damageable.damage + 1) == inHand.type.maxDurability.toInt()) {
                                val name = if (heldItemType == Material.DIAMOND_PICKAXE)
                                    "picareta monstra"
                                else
                                    "pá monstra"

                                e.player.sendMessage("§cCuidado, a sua $name irá quebrar! Para te proteger, a gente te bloqueou de quebrar mais blocos para você poder reparar ela!")
                                return
                            }

                            damageable.damage = damageable.damage + 1
                            inHand.itemMeta = damageable as ItemMeta
                        }

                        if (damageable.damage > inHand.type.maxDurability) {
                            e.player.inventory.removeItem(inHand)
                        }

                        val drops = location.block.getDrops(inHand).toMutableList()
                        val exp = BlockUtils.getExpCount(location.block, enchantmentLevel)

                        if (inHand canMineRubyFrom e.block.type) drops.add(CustomItems.RUBY.clone())
                        allDrops.addAll(drops)

                        // Because we want to create an item entity reference, we need to make it ourselves with NMS
                        // We are going to mimick how items normally spawn when dropping, with the FakeBlockDropItemEvent too, to trigger all plugins that
                        // are using that event
                        val craftWorld = (location.world as CraftWorld).handle

                        val dropsAsItems = drops.map {
                            // Using "dropItemNaturally" is kinda bad because the item can stay inside of blocks
                            ItemEntity(craftWorld, location.x, location.y, location.z, CraftItemStack.asNMSCopy(it))
                                .bukkitEntity as Item
                        }

                        if (isPicaretaMonstra) {
                            m.doMcMMOStuffMining(
                                e.player,
                                location.block.state,
                                dropsAsItems,
                                isSilky
                            )
                        } else {
                            m.doMcMMOStuffExcavation(
                                e.player,
                                location.block.state,
                                dropsAsItems
                            )
                        }

                        // For my friend mcMMO xoxo, and for the magnet stuff too
                        val fakeEvent = DreamPicaretaMonstra.FakeBlockDropItemEvent(
                            location.block,
                            location.block.state,
                            e.player,
                            dropsAsItems
                        )
                        Bukkit.getPluginManager().callEvent(fakeEvent)
                        fakeEvent.items.forEach {
                            craftWorld.addFreshEntity((it as CraftItem).handle)
                        }

                        // Do not update physics, this tries to avoid a lot of "notify()" calls
                        // See: https://cdn.discordapp.com/attachments/513405772911345664/869387512924209182/wLnOZAeMZ5.sparkprofile
                        location.block.setType(Material.AIR, false)

                        if (exp > 0 && !isSilky) {
                            val orb = location.block.world.spawnEntity(center, EntityType.EXPERIENCE_ORB) as ExperienceOrb
                            orb.experience = exp
                        }

                        if (location.block.type != Material.AIR) {
                            if (shouldPlay) {
                                e.player.world.playSound(broken.location, Sound.ENTITY_PLAYER_LEVELUP, 0.15f, 1.25f)
                                shouldPlay = false
                            }
                            location.world.spawnParticle(Particle.VILLAGER_HAPPY, center, 1)
                            location.world.spawnParticle(Particle.FIREWORKS_SPARK, center, 1)
                        } else {
                            location.world.spawnParticle(Particle.SPELL_WITCH, center, 1)
                        }
                    } else {
                        if (location.block.type === Material.AIR) {
                            continue
                        }
                        location.world.spawnParticle(Particle.VILLAGER_ANGRY, center, 1)
                    }
                }
            }
        }
    }
}