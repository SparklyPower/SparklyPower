package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class CheckBanCommand : RemoteCommandExecutorCommand(
		"checkban",
		listOf(),
		"dreamnetworkbans.checkban",
		"checkban",
		Server.PERFECTDREAMS_BUNGEE
)