package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class WarnCommand : RemoteCommandExecutorCommand(
		"warn",
		listOf(),
		"dreamnetworkbans.warn",
		"warn",
		Server.PERFECTDREAMS_BUNGEE
)