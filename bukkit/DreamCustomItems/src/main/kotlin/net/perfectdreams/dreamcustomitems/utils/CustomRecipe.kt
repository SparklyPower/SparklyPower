package net.perfectdreams.dreamcustomitems.utils

import net.kyori.adventure.text.Component
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
    val replacePrismarineShardWithRuby: Boolean,
    val recipe: Recipe
) : CustomRecipe() {
    override val result = recipe.result
}