package net.perfectdreams.commands.bukkit

import net.perfectdreams.commands.BaseCommand

open class SparklyCommand(override val labels: Array<out String>, val permission: String? = null) : BaseCommand {
	lateinit var backedCommandWrapper: BukkitCommandWrapper
	override val subcommands: MutableList<BaseCommand> = mutableListOf()

	init {
		registerSubcommands()
	}
}