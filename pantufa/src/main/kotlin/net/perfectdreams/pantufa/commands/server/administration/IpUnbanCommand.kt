package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class IpUnbanCommand : RemoteCommandExecutorCommand(
		"ipunban",
		listOf("desbanirip"),
		"dreamnetworkbans.ipunban",
		"ipunban",
		Server.PERFECTDREAMS_BUNGEE
)