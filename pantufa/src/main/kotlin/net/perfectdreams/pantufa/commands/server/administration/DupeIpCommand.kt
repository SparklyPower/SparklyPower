package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class DupeIpCommand : RemoteCommandExecutorCommand(
		"dupeip",
		listOf(),
		"dreamnetworkbans.dupeip",
		"dupeip",
		Server.PERFECTDREAMS_BUNGEE
)