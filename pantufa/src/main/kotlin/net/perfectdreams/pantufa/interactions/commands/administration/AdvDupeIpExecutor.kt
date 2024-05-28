package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Server

class AdvDupeIpExecutor(
    pantufa: PantufaBot
) : AdminConsoleBungeeExecutor(
    pantufa,
    "dreamnetworkbans.advdupeip",
    "advdupeip",
    Server.PERFECTDREAMS_BUNGEE
)