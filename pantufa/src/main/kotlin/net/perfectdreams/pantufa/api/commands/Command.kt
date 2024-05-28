package net.perfectdreams.pantufa.api.commands

import net.perfectdreams.pantufa.PantufaBot

open class Command<T : CommandContext>(
		val pantufa: PantufaBot,
		val labels: List<String>,
		val commandName: String,
		val executor: (suspend T.() -> (Unit))
) {
	var needsToUploadFiles = false
	var hideInHelp = false
	var hasCommandFeedback = true
	var sendTypingStatus = false
	var canUseInPrivateChannel = false
	var onlyOwner = false
	var similarCommands: List<String> = listOf()

	open val cooldown = 2_500
	var executedCount = 0
	// var lorittaPermissions = listOf()

}