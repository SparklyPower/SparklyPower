package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.holders.CustomItemRecipeHolder
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import javax.lang.model.element.VariableElement
import kotlin.reflect.KType
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

object CustomItemRecipeCommand : DSLCommandBase<DreamCustomItems> {
    override fun command(plugin: DreamCustomItems) = create(
            listOf("customitemrecipe")
    ) {
        executes {
            val itemName = args.getOrNull(0)

            val customItems = CustomItems::class.members.filter {
                it.returnType.toString().contains("ItemStack") && it.name != "RUBY"
            }

            if (itemName == null) {
                var customItemsNameList = ""

                customItems.forEach { customItemsNameList += "§e${it.name.toLowerCase()}§f|" }

                sender.sendMessage("§e/craft §f(${customItemsNameList.removeSuffix("|")})")

                return@executes
            }

            if (customItems.firstOrNull { it.name == itemName.toUpperCase() } == null) {
                sender.sendMessage("§cEste item não existe!")

                return@executes
            }

            val recipeInvetory = Bukkit.createInventory(CustomItemRecipeHolder(), InventoryType.WORKBENCH, "Crafting")

            val key = NamespacedKey(plugin, itemName.toLowerCase())

            val itemRecipe = Bukkit.getServer().getRecipe(key) as ShapedRecipe

            val itemResult = customItems.first { it.name == itemName.toUpperCase() }.call(CustomItems) as ItemStack

            recipeInvetory.setItem(0, itemResult.clone())

            var inventoryIndex = 1

            itemRecipe.ingredientMap.forEach {

                if (it.value == null) {
                    recipeInvetory.setItem(inventoryIndex, ItemStack(Material.AIR))
                } else if (it.value.type == Material.PRISMARINE_SHARD) {
                    recipeInvetory.setItem(inventoryIndex, CustomItems.RUBY)
                } else {
                    recipeInvetory.setItem(inventoryIndex, it.value)
                }

                inventoryIndex += 1
            }

			player.openInventory(recipeInvetory)
        }
    }
}
