package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class BanCommand : RemoteCommandExecutorCommand(
		"ban",
		listOf("banir"),
		"dreamnetworkbans.ban",
		"ban",
		Server.PERFECTDREAMS_BUNGEE
)