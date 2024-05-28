package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Server

class GeoIpExecutor(
    pantufa: PantufaBot
) : AdminConsoleBungeeExecutor(
    pantufa,
    "dreamnetworkbans.geoip",
    "geoip",
    Server.PERFECTDREAMS_BUNGEE
)