package net.perfectdreams.dreamclubes.commands.declarations

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.ClubeChatExecutor
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class ClubeChatCommand(val m: DreamClubes) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf(".")) {
        executor = ClubeChatExecutor(m)
    }
}