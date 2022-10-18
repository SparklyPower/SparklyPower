package net.perfectdreams.dreamlagstuffrestrictor.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamlagstuffrestrictor.commands.EntitySearchAllExecutor
import net.perfectdreams.dreamlagstuffrestrictor.commands.EntitySearchDumpToFileExecutor
import net.perfectdreams.dreamlagstuffrestrictor.commands.EntitySearchKillOutsideExecutor

class EntitySearchDeclaration : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("entitysearch")) {
        subcommand(listOf("all")) {
            permissions = listOf("dreamlagstuffrestrictor.entitysearch")
            executor = EntitySearchAllExecutor()
        }

        subcommand(listOf("dumpentitiestofile")) {
            permissions = listOf("dreamlagstuffrestrictor.entitysearch")
            executor = EntitySearchDumpToFileExecutor()
        }

        subcommand(listOf("killoutside")) {
            permissions = listOf("dreamlagstuffrestrictor.killoutside")
            executor = EntitySearchKillOutsideExecutor()
        }
    }
}