package net.perfectdreams.commands.bukkit

import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.dsl.DSLExecutorWrapper

open class SparklyDSLCommand(override val labels: Array<out String>, dslSubcommands: List<BaseDSLCommand>) : SparklyCommand(labels), BaseDSLCommand {
	override val executors: List<DSLExecutorWrapper> = mutableListOf()

	init {
		// lol nope, vamos ignorar todos os subcomandos registrados pela classe principal, elas são chatas!
		subcommands.clear()

		// E colocar todos os subcomandos de DSL após iniciar
		subcommands.addAll(dslSubcommands)

		// Deste jeito ainda é possível usar o "subcommands" para adicionar subcomandos de outras classes! Yay!
	}
}