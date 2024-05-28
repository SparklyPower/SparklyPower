package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Server

class UnbanExecutor(
    pantufa: PantufaBot
) : AdminConsoleBungeeExecutor(
    pantufa,
    "dreamnetworkbans.unban",
    "unban",
    Server.PERFECTDREAMS_BUNGEE
)