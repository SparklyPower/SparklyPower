package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.holders.CustomItemRecipeHolder
import net.perfectdreams.dreamcustomitems.utils.CustomCraftingRecipe
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomTextualRecipe
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ShapedRecipe

class CustomItemRecipesExecutor(val m: DreamCustomItems) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val customItems = m.customRecipes
        val inventorySize = ((customItems.size / 9.0).toInt() + 1) * 9

        val menu = createMenu(inventorySize, "§cItens customizados") {
            for ((value, customItemRecipeWrapper) in customItems.withIndex()) {
                slot(value, 0) {
                    this.item = customItemRecipeWrapper.result

                    onClick {
                        it.closeInventory()

                        when (customItemRecipeWrapper) {
                            is CustomCraftingRecipe -> {
                                val recipeInventory = Bukkit.createInventory(
                                    CustomItemRecipeHolder(),
                                    InventoryType.WORKBENCH,
                                    "Crafting"
                                )

                                val ruby = CustomItems.RUBY
                                    .clone()
                                    .lore(
                                        "§7Rubí pode ser encontrado quebrando",
                                        "§7minérios de redstone, cada minério",
                                        "§7possui §8${CustomItems.RUBY_DROP_CHANCE}% §7de chance de",
                                        "§7dropar um rubi!"
                                    )

                                val itemRecipe = customItemRecipeWrapper.recipe

                                recipeInventory.setItem(0, itemRecipe.result) // Recipe result

                                var inventoryIndex = 1

                                if (itemRecipe is ShapedRecipe) {
                                    // Parse the recipe items into the ingredient map
                                    itemRecipe.ingredientMap.forEach { ingredient ->
                                        when {
                                            customItemRecipeWrapper.replacePrismarineShardWithRuby && ingredient.value.type == Material.PRISMARINE_SHARD -> {
                                                recipeInventory.setItem(inventoryIndex, ruby)
                                            }

                                            else -> {
                                                recipeInventory.setItem(inventoryIndex, ingredient.value)
                                            }
                                        }

                                        inventoryIndex += 1
                                    }
                                } else {
                                    error("I don't know how to handle a $itemRecipe!")
                                }

                                player.openInventory(recipeInventory)
                            }
                            is CustomTextualRecipe -> {
                                for (component in customItemRecipeWrapper.explanation) {
                                    player.sendMessage(component)
                                }
                            }
                        }
                    }
                }
            }
        }

        menu.sendTo(player)
    }
}