package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class IpBanCommand : RemoteCommandExecutorCommand(
		"ipban",
		listOf("banirip"),
		"dreamnetworkbans.ipban",
		"ipban",
		Server.PERFECTDREAMS_BUNGEE
)