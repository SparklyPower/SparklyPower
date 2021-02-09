package net.perfectdreams.dreamcustomitems.utils

import com.gmail.nossr50.util.player.UserManager
import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.meta.ItemMeta

class SuperFurnace(val m: DreamCustomItems, val location: Location) {
    var ticksRunning = 0
    var running = false
    var scheduler: CoroutineTask? = null
    var player: Player? = null

    val inventory = Bukkit.createInventory(SuperFurnaceHolder(this), 36, "Super Fornalha").apply {
        repeat(36) {
            when(it) {
                !in listOf(0, 1, 2, 3, 4, 5, 18, 19, 20, 21, 22, 23, 27,28, 29, 30, 31, 32) -> {
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

    fun updateInventory() {
        if (running) {
            val minutes = (ticksRunning/20) / 60
            val seconds = (ticksRunning/20) % 60

            inventory.setItem(
                    7,
                    ItemStack(Material.GREEN_STAINED_GLASS_PANE)
                            .rename("§a§lSuper Fornalha ligada §8[§7Falta ${minutes}m${seconds}s§8]")
            )
        } else {
            inventory.setItem(
                    7,
                    ItemStack(Material.RED_STAINED_GLASS_PANE)
                            .rename("§a§lLigar a Super Fornalha")
            )
        }
    }

    fun playersNearSuperFurnace() = location.world.getNearbyPlayers(location, 10.0)

    fun convertToSmeltedItem(itemStack: ItemStack): ItemStack? {
        var result: ItemStack? = null
        var newMcMMOAmount = 0
        val iter: Iterator<Recipe> = Bukkit.recipeIterator()
        val mcmmoPlayer = UserManager.getPlayer(player)

        while (iter.hasNext()) {
            val recipe: Recipe = iter.next() as? FurnaceRecipe ?: continue
            if ((recipe as FurnaceRecipe).input.type !== itemStack.type) continue

            result = mcmmoPlayer?.smeltingManager?.smeltProcessing(itemStack, recipe.result)

			repeat(itemStack.amount) {
				newMcMMOAmount += mcmmoPlayer?.smeltingManager?.smeltProcessing(itemStack, recipe.result)?.amount ?: 0
			}
            
            break
        }
        result?.amount = newMcMMOAmount
        return result
    }

    fun open(furnacePlayer: Player) {
        furnacePlayer.openInventory(inventory)
        furnacePlayer.playSound(location, "perfectdreams.sfx.microwave.open", SoundCategory.BLOCKS, 1f, 1f)

        player = furnacePlayer
    }

    fun start() {
        listOf(18, 19, 20, 21, 22, 23, 27,28, 29, 30, 31, 32).forEach {
            if (inventory.getItem(it) != null) {
                player?.sendMessage("§cTire os itens dos slots de saída da super fornalha antes de ligar ela!")
                return
            }
        }

        playersNearSuperFurnace().forEach {
            it.playSound(location, "perfectdreams.sfx.microwave.button", SoundCategory.BLOCKS, 1f, 1f)
        }

        ticksRunning = 4800
        running = true
        updateInventory()

        scheduler?.cancel()

        scheduler = scheduler().schedule(m) {
            while (running) {
                playersNearSuperFurnace().forEach {
                    it.playSound(location, "perfectdreams.sfx.microwave.spin", SoundCategory.BLOCKS, 1f, 1f)
                }


                waitFor(20)
                ticksRunning -= 20

                updateInventory()

                if (0 >= ticksRunning) {
                    playersNearSuperFurnace().forEach {
                        it.stopSound("perfectdreams.sfx.microwave.spin", SoundCategory.BLOCKS)
                    }

                    running = false
                    finish()
                    updateInventory()

                    break
                }
            }
        }
    }

    fun finish() {
        for (i in 0..5) {
            val item = inventory.getItem(i)

            if (item != null) {
                val smeltedItem = convertToSmeltedItem(item)

                if (smeltedItem != null) {
                    inventory.setItem(i, null)

                    if (smeltedItem.amount > 64) {
                        if (smeltedItem.amount % 64 == 0) smeltedItem.amount = 64 else smeltedItem.amount %= 64
                        inventory.setItem(i+27, smeltedItem)

                        smeltedItem.amount = 64
                        inventory.setItem(i+18, smeltedItem)
                    } else {
                        inventory.setItem(i+18, smeltedItem)
                    }

                }
            }
        }
    }

    fun stop() {
        running = false
        updateInventory()

        playersNearSuperFurnace().forEach {
            it.stopSound("perfectdreams.sfx.microwave.spin", SoundCategory.BLOCKS)
        }
    }

}