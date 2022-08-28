package net.perfectdreams.dreamcustomitems.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcustomitems.commands.CustomItemsGiveExecutor
import net.perfectdreams.dreamcustomitems.commands.CustomItemsMetaExecutor

object DreamCustomItemsCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamcustomitems")) {
        permissions = listOf("dreamcustomitems.setup")

        subcommand(listOf("give")) {
            executor = CustomItemsGiveExecutor()
            permissions = listOf("dreamcustomitems.setup")
        }

        subcommand(listOf("meta")) {
            executor = CustomItemsMetaExecutor()
            permissions = listOf("dreamcustomitems.setup")
        }
    }
}