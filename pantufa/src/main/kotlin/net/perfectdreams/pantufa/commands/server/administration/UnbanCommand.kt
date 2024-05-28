package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class UnbanCommand : RemoteCommandExecutorCommand(
		"unban",
		listOf(),
		"dreamnetworkbans.unban",
		"unban",
		Server.PERFECTDREAMS_BUNGEE
)