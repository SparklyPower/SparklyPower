package net.perfectdreams.pantufa.commands.server.administration

import net.perfectdreams.pantufa.utils.Server

class GeoIpCommand : RemoteCommandExecutorCommand(
		"geoip",
		listOf(),
		"dreamnetworkbans.geoip",
		"geoip",
		Server.PERFECTDREAMS_BUNGEE
)