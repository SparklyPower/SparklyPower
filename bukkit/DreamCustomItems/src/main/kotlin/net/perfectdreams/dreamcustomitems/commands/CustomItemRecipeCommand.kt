package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.holders.CustomItemRecipeHolder
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import kotlin.reflect.KCallable

object CustomItemRecipeCommand : DSLCommandBase<DreamCustomItems> {
    override fun command(plugin: DreamCustomItems) = create(
            listOf("customrecipes", "customitems")
    ) {
        executes {
            val itemName = args.getOrNull(0)

            val customItemsList = customItems(true)

            if (itemName == null) {
                val invetorySize = if (customItemsList.size % 9 == 0) customItemsList.size
                else customItemsList.size + 9 - (customItemsList.size % 9)

                val menu = createMenu(invetorySize, "§cItens customizados") {
                    for ((value, item) in customItemsList.withIndex()) {
                        slot(value, 0) {
                            val selectedItem = getNewCustomItem(item.name, customItemsList)

                            this.item = selectedItem

                            onClick {
                                it.closeInventory()

                                val recipeInvetory = Bukkit.createInventory(
                                        CustomItemRecipeHolder(),
                                        InventoryType.WORKBENCH,
                                        "Crafting"
                                )

                                val key = NamespacedKey(plugin, item.name.toLowerCase())
                                val itemRecipe = Bukkit.getServer().getRecipe(key) as ShapedRecipe
                                val ruby = getNewCustomItem("RUBY", customItems(false))

                                recipeInvetory.setItem(0, selectedItem) // Recipe result

                                var inventoryIndex = 1

                                itemRecipe.ingredientMap.forEach { ingredient ->
                                    when {
                                        ingredient.value == null -> {
                                            recipeInvetory.setItem(inventoryIndex, ItemStack(Material.AIR))
                                        }
                                        ingredient.value.type == Material.PRISMARINE_SHARD -> {
                                            recipeInvetory.setItem(inventoryIndex, ruby)
                                        }
                                        else -> {

                                            ingredient.value.addUnsafeEnchantment(Enchantment.LUCK, 1)
                                            ingredient.value.addItemFlags(ItemFlag.HIDE_ENCHANTS)

                                            recipeInvetory.setItem(inventoryIndex, ingredient.value)
                                        }
                                    }

                                    inventoryIndex += 1
                                }

                                player.openInventory(recipeInvetory)
                            }
                        }
                    }
                }

                menu.sendTo(player)
                return@executes
            }
        }
    }

    fun customItems(ignoreNonCraftableItems: Boolean): List<KCallable<*>> {

        return if (ignoreNonCraftableItems) {
            CustomItems::class.members.filter {
                it.returnType.toString().contains("ItemStack") && it.name != "RUBY"
            }
        } else {
            CustomItems::class.members.filter {
                it.returnType.toString().contains("ItemStack")
            }
        }
    }

    fun getNewCustomItem(itemName: String, customItems: List<KCallable<*>>): ItemStack {

        val newItem = (customItems.first { it.name == itemName.toUpperCase() }.call(CustomItems) as ItemStack).clone()

        val newItemMeta = newItem.itemMeta

        if (newItem.type == Material.PRISMARINE_SHARD) {
            newItemMeta.lore = listOf(
                    "§7Rubí pode ser encontrado quebrando",
                    "§7minérios de redstone, cada minério",
                    "§7possui §8${CustomItems.RUBY_DROP_CHANCE}% §7de chance de",
                    "§7dropar um rubi!")
        }

        newItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        newItem.itemMeta = newItemMeta

        return newItem
    }
}
