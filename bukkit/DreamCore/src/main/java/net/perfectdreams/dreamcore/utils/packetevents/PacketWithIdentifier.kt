package net.perfectdreams.dreamcore.utils.packetevents

import net.minecraft.network.protocol.Packet

data class PacketWithIdentifier(
    val identifier: String,
    val packet: Packet<*>
)