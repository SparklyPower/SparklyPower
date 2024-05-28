package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class AdvDupeIpCommand : RemoteCommandExecutorCommand(
		"advdupeip",
		listOf(),
		"dreamnetworkbans.advdupeip",
		"advdupeip",
		Server.PERFECTDREAMS_BUNGEE
)