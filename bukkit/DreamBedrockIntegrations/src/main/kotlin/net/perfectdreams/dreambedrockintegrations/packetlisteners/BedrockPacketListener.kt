package net.perfectdreams.dreambedrockintegrations.packetlisteners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedChatComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreambedrockintegrations.utils.isBedrockClient

class BedrockPacketListener(val m: DreamBedrockIntegrations) : PacketAdapter(
    m,
    ListenerPriority.NORMAL, // Listener priority
    listOf(
        PacketType.Play.Server.OPEN_WINDOW
    )
) {
    override fun onPacketSending(event: PacketEvent) {
        // We only care if the player is a bedrock client
        if (!event.player.isBedrockClient)
            return

        val titleAsJson = event.packet.chatComponents.read(0)
            .json

        val adventureComponent = GsonComponentSerializer.gson().deserialize(titleAsJson)
        val matchedTransformer = m.inventoryTitleTransformers.firstOrNull {
            it.matchInventory.invoke(adventureComponent)
        } ?: return

        event.packet.chatComponents.write(
            0,
            WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(matchedTransformer.newInventoryName.invoke(adventureComponent)))
        )
    }
}