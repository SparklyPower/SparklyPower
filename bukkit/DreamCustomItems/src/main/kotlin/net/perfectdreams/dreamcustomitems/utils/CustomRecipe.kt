package net.perfectdreams.dreamcustomitems.utils

import net.kyori.adventure.text.Component
import net.perfectdreams.dreamcore.utils.lore
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.plugin.Plugin

sealed class CustomRecipe {
    abstract val plugin: Plugin
    abstract val result: ItemStack
}

data class CustomTextualRecipe(
    override val plugin: Plugin,
    override val result: ItemStack,
    val explanation: List<Component>
) : CustomRecipe()

data class CustomCraftingRecipe(
    override val plugin: Plugin,
    val itemRemapper: (ItemStack) -> (ItemStack) = { ItemStack(it) },
    val checkRemappedItems: Boolean,
    val recipe: Recipe
) : CustomRecipe() {
    companion object {
        val RUBY_WITH_RECIPE_DESCRIPTION = CustomItems.RUBY
            .clone()
            .lore(
                "§7Rubí pode ser encontrado quebrando",
                "§7minérios de redstone, cada minério",
                "§7possui §8${CustomItems.RUBY_DROP_CHANCE}% §7de chance de",
                "§7dropar um rubi!"
            )

        val RUBY_REMAP: (ItemStack) -> (ItemStack) = {
            if (it.type == Material.PRISMARINE_SHARD)
                RUBY_WITH_RECIPE_DESCRIPTION
            else ItemStack(it)
        }
    }

    override val result = recipe.result
}