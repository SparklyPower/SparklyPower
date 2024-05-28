package net.perfectdreams.pantufa.interactions.commands.administration

import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Server

class FingerprintExecutor(
    pantufa: PantufaBot
) : AdminConsoleBungeeExecutor(
    pantufa,
    "dreamnetworkbans.fingerprint",
    "fingerprint",
    Server.PERFECTDREAMS_BUNGEE
)