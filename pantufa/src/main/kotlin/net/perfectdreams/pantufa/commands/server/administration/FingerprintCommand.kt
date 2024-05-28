package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class FingerprintCommand : RemoteCommandExecutorCommand(
		"fingerprint",
		listOf(),
		"dreamnetworkbans.fingerprint",
		"fingerprint",
		Server.PERFECTDREAMS_BUNGEE
)