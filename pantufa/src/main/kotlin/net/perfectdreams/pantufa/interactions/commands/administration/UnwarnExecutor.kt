package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Server

class UnwarnExecutor(
    pantufa: PantufaBot
) : AdminConsoleBungeeExecutor(
    pantufa,
    "dreamnetworkbans.unwarn",
    "unwarn",
    Server.PERFECTDREAMS_BUNGEE
)