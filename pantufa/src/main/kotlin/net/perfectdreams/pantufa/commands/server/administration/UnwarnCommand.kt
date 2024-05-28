package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class UnwarnCommand : RemoteCommandExecutorCommand(
		"unwarn",
		listOf(),
		"dreamnetworkbans.unwarn",
		"unwarn",
		Server.PERFECTDREAMS_BUNGEE
)