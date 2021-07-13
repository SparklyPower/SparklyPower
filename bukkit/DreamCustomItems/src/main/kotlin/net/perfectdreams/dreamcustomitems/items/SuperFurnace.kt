package net.perfectdreams.dreamcustomitems.items

import com.gmail.nossr50.config.Config
import com.gmail.nossr50.config.experience.ExperienceConfig
import com.gmail.nossr50.datatypes.experience.XPGainReason
import com.gmail.nossr50.datatypes.experience.XPGainSource
import com.gmail.nossr50.datatypes.player.McMMOPlayer
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.util.player.UserManager
import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.holders.SuperFurnaceHolder
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.FurnaceRecipe

class SuperFurnace(val m: DreamCustomItems, val location: Location) {
    var ticksRunning = 0
    var running = false
    var scheduler: CoroutineTask? = null
    var furnacePlayer: Player? = null

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

    // MCMMO SMELTING XP
    fun getResourceXp(smelting: ItemStack): Int {
        return if (mcMMO.getModManager().isCustomOre(smelting.type)) mcMMO.getModManager().getBlock(smelting.type).smeltingXpGain else ExperienceConfig.getInstance().getXp(PrimarySkillType.SMELTING, smelting.type)
    }

    // MCMMO DOUBLE SMELTING
    fun smeltProcessing(smelting: ItemStack, result: ItemStack, mcmmoPlayer: McMMOPlayer): ItemStack {
        mcmmoPlayer.smeltingManager.applyXpGain(getResourceXp(smelting).toFloat(), XPGainReason.PVE, XPGainSource.PASSIVE)

        return if (Config.getInstance().getDoubleDropsEnabled(PrimarySkillType.SMELTING, result.type) && mcmmoPlayer.smeltingManager.isSecondSmeltSuccessful) {
            val newResult: ItemStack = result.clone()
            newResult.amount = result.amount + 1

            newResult;
        } else {
            result;
        }
    }

    fun convertToSmeltedItem(item: ItemStack): ItemStack? {
        val iter: Iterator<Recipe> = Bukkit.recipeIterator()
        var result: ItemStack? = null
        var newAmount = 0

        while (iter.hasNext()) {
            val recipe: Recipe = iter.next() as? FurnaceRecipe ?: continue

            if ((recipe as FurnaceRecipe).input.type !== item.type) continue

            result = recipe.result.asQuantity(item.amount)

            repeat(item.amount) {
                newAmount += smeltProcessing(item, recipe.result, UserManager.getPlayer(furnacePlayer)).amount
            }

            break
        }

        if (result == null) return null

        result.amount += newAmount

        return result
    }

    fun open(player: Player) {
        player.openInventory(inventory)
        player.playSound(location, "perfectdreams.sfx.microwave.open", SoundCategory.BLOCKS, 1f, 1f)
    }

    fun start(whoStarted: Player) {

        furnacePlayer = whoStarted

        listOf(18, 19, 20, 21, 22, 23, 27,28, 29, 30, 31, 32).forEach {
            if (inventory.getItem(it) != null) {
                furnacePlayer?.sendMessage("§8[§6§lSuper Fornalha§8] §cTire os itens dos slots de saída da super fornalha antes de ligar ela!")
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
                    it.playSound(location, Sound.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1f, 1f)
                }


                waitFor(20)
                ticksRunning -= 20

                updateInventory()

                if (0 >= ticksRunning) {
                    playersNearSuperFurnace().forEach {
                        it.stopSound(Sound.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS)
                    }

                    running = false

                    var canFinishSmelting = true

                    listOf(18, 19, 20, 21, 22, 23, 27,28, 29, 30, 31, 32).forEach {
                        if (inventory.getItem(it) != null) {
                            furnacePlayer?.sendMessage("§8[§6§lSuper Fornalha§8] §c§lSeus itens já estão prontos, mas não há espaço para eles! Tire os itens dos slots de saída da super fornalha e ligue ela novamente.")
                            updateInventory()
                            canFinishSmelting = false
                        }
                    }

                    if (canFinishSmelting) {
                        finish()
                        updateInventory()
                    }

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

        furnacePlayer?.sendMessage("§8[§6§lSuper Fornalha§8] §c§lSeus itens já estão prontos!")
    }

    fun stop() {
        running = false
        updateInventory()

        playersNearSuperFurnace().forEach {
            it.stopSound(Sound.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS)
        }
    }

}
