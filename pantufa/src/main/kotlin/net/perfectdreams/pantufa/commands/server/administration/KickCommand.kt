package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class KickCommand : RemoteCommandExecutorCommand(
		"kick",
		listOf(),
		"dreamnetworkbans.kick",
		"kick",
		Server.PERFECTDREAMS_BUNGEE
)