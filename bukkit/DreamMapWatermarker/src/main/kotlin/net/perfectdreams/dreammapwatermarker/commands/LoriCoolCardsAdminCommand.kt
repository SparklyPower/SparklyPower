package net.perfectdreams.dreammapwatermarker.commands

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreammapwatermarker.DreamMapWatermarker


class LoriCoolCardsAdminCommand(val m: DreamMapWatermarker) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("divinefigurittas")) {
        permission = "dreammapmaker.figurittas.setup"
        executor = LoriCoolCardsCommand.FigurittasExecutor(m, true)
    }
}