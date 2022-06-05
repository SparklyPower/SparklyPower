package net.perfectdreams.dreambedrockintegrations.utils

import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val Player.isBedrockClient: Boolean
    get() = (Bukkit.getPluginManager().getPlugin("DreamBedrockIntegrations") as DreamBedrockIntegrations)
        .geyserUsers
        .contains(this.uniqueId)