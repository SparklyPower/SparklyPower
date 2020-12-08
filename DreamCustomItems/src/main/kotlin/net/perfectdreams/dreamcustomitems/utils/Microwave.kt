package net.perfectdreams.dreamcustomitems.utils

import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.meta.ItemMeta

class Microwave(val m: DreamCustomItems, val location: Location) {
    var ticksRunning = 0
    var running = false
    var scheduler: CoroutineTask? = null

    val inventory = Bukkit.createInventory(MicrowaveHolder(this), 9, "Micro-ondas")
        .apply {
            repeat(9) {
                when (it) {
                    !in 3..5 -> {
                        setItem(
                            it,
                            ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE).meta<ItemMeta> {
                                setCustomModelData(1)
                                setDisplayName("§f")
                            }
                        )
                    }
                }
            }
        }

    init {
        updateInventory()
    }

    fun open(player: Player) {
        player.openInventory(inventory)
        player.playSound(location, "perfectdreams.sfx.microwave.open", SoundCategory.BLOCKS, 1f, 1f)
    }

    fun stop() {
        running = false
        updateInventory()

        playersNearMicrowave().forEach {
            it.playSound(location, "perfectdreams.sfx.microwave.button", SoundCategory.BLOCKS, 1f, 1f)
            it.stopSound("perfectdreams.sfx.microwave.spin", SoundCategory.BLOCKS)
        }
    }

    fun updateInventory() {
        if (running) {
            inventory.setItem(
                7,
                ItemStack(Material.GREEN_STAINED_GLASS_PANE)
                    .rename("§a§lMicro-ondas ligado §8[§7Falta ${ticksRunning / 20}s§8]")
            )
        } else {
            inventory.setItem(
                7,
                ItemStack(Material.RED_STAINED_GLASS_PANE)
                    .rename("§a§lLigar o Micro-ondas")
            )
        }
    }

    fun start() {
        playersNearMicrowave().forEach {
            it.playSound(location, "perfectdreams.sfx.microwave.button", SoundCategory.BLOCKS, 1f, 1f)
        }

        ticksRunning = 300
        running = true
        updateInventory()

        scheduler?.cancel()

        scheduler = scheduler().schedule(m) {
            while (running) {
                playersNearMicrowave().forEach {
                    it.playSound(location, "perfectdreams.sfx.microwave.spin", SoundCategory.BLOCKS, 1f, 1f)
                }

                waitFor(20)
                ticksRunning -= 20

                if (ticksRunning == 100) {
                    for (i in 3..5) {
                        val item = inventory.getItem(i)

                        if (item != null && item.type == Material.IRON_INGOT) {
                            stop()
                            m.microwaves.remove(location)
                            location.world.createExplosion(location, 1f)
                            location.block.type = Material.AIR
                            return@schedule
                        }
                    }
                }

                updateInventory()

                if (0 >= ticksRunning) {
                    playersNearMicrowave().forEach {
                        it.stopSound("perfectdreams.sfx.microwave.spin", SoundCategory.BLOCKS)
                    }

                    running = false
                    finish()
                    updateInventory()

                    repeat(3) {
                        playersNearMicrowave().forEach {
                            it.playSound(location, "perfectdreams.sfx.microwave.stop", SoundCategory.BLOCKS, 1f, 1f)
                        }

                        waitFor(30L)
                    }
                    break
                }
            }
        }
    }

    fun finish() {
        for (i in 3..5) {
            val item = inventory.getItem(i)

            if (item != null) {
                val smeltedItem = convertToSmeltedItem(item)

                if (smeltedItem != null) {
                    inventory.setItem(i, smeltedItem)
                }
            }
        }
    }

    fun playersNearMicrowave() = location.world.getNearbyPlayers(location, 10.0)

    fun convertToSmeltedItem(itemStack: ItemStack): ItemStack? {
        var result: ItemStack? = null
        val iter: Iterator<Recipe> = Bukkit.recipeIterator()
        while (iter.hasNext()) {
            val recipe: Recipe = iter.next() as? FurnaceRecipe ?: continue
            if ((recipe as FurnaceRecipe).input.type !== itemStack.type) continue
            result = recipe.result
            break
        }
        result?.amount = itemStack.amount
        return result
    }
}