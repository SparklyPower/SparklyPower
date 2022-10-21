package net.perfectdreams.dreamcustomitems.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.commands.CustomItemRecipesExecutor
import net.perfectdreams.dreamcustomitems.commands.CustomItemsGiveExecutor
import net.perfectdreams.dreamcustomitems.commands.CustomItemsMetaExecutor

class CustomItemRecipesCommand(val m: DreamCustomItems) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("customrecipes", "customitems")) {
        permissions = listOf("dreamcustomitems.setup")
        executor = CustomItemRecipesExecutor(m)
    }
}