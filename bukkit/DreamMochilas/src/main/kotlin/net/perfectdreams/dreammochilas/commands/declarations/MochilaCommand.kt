package net.perfectdreams.dreammochilas.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreammochilas.commands.*

object MochilaCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("mochila")) {
        subcommand(listOf("shopall")) {
            subcommand(listOf("sell")) {
                executor = ToggleShopAllExecutor
            }
        }

        subcommand(listOf("id")) {
            executor = GetMochilaIdExecutor
        }
    }
}