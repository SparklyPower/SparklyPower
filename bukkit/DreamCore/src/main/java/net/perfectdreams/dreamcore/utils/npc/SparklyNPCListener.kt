package net.perfectdreams.dreamcore.utils.npc

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.GameType
import net.perfectdreams.dreamcore.event.PlayerScoreboardCreatedEvent
import net.perfectdreams.dreamcore.utils.JVMUnsafeUtils
import net.perfectdreams.dreamcore.utils.get
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class SparklyNPCListener(val m: SparklyNPCManager) : Listener {
    @EventHandler
    fun onEntitiesLoad(event: EntitiesLoadEvent) {
        for (entity in event.entities) {
            // Is this an NPC?
            if (entity.persistentDataContainer.get(m.npcKey)) {
                // Is the entity's unique ID present in the NPC Entities list?
                if (!m.npcEntities.containsKey(entity.uniqueId)) {
                    // If not, we are going to delete it!
                    m.m.logger.warning("Deleting entity ${entity.uniqueId} because their ID isn't present in the NPC list!")

                    // Bail out!
                    entity.remove()
                }
            }
        }
    }

    @EventHandler
    fun onDisable(event: PluginDisableEvent) {
        val entitiesToBeDeleted = mutableSetOf<UUID>()

        m.npcEntities.forEach { (id, data) ->
            if (data.owner == event.plugin) {
                // Delete all NPCs when the plugin is disabled
                data.remove()
            }
        }

        entitiesToBeDeleted.forEach {
            m.npcEntities.remove(it)
        }
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
        val npcData = m.npcEntities[event.entity.uniqueId]
        if (npcData != null) {
            npcData.remove()
            m.npcEntities.remove(npcData.uniqueId)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        /* (event.player as CraftPlayer).handle
            .connection
            .connection
            .channel
            .pipeline()
            .forEach {
                println(it.key + " - " + it.value)
            } */

        val nmsPlayer = (event.player as CraftPlayer).handle

        // This is per player! It is an entity ID to Entity UUID reference
        // We don't really *need* to store a SparklyNPC reference (it would be dead btw because we had removed the NPC), we just need this to send
        // a remove player packet
        val spawnedNPCEntities = mutableMapOf<Int, UUID>()

        // Create Netty pipeline to intercept packets
        nmsPlayer
            .connection
            .connection
            .channel
            .pipeline()
            .addBefore(
                "bundler",
                "sparklynpc-handler",
                object: ChannelDuplexHandler() {
                    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
                        // println(msg::class.java)

                        if (msg is ClientboundBundlePacket) {
                            /* println("Bundle Packet has...")
                            msg.subPackets().forEach {
                                println("> ${it::class.java}")
                            } */

                            val newSubPackets = mutableListOf<Packet<in ClientGamePacketListener>>()
                            for (subPacket in msg.subPackets()) {
                                // It *seems* that vanilla entities are always sent in a bundle packet
                                if (subPacket is ClientboundAddEntityPacket) {
                                    if (subPacket.type == EntityType.HUSK) {
                                        val npcData = m.npcEntities[subPacket.uuid]

                                        // println("NPC data: ${npcData}")
                                        if (npcData != null) {
                                            val gp = GameProfile(subPacket.uuid, npcData.fakePlayerName)
                                            m.m.logger.info { "Intercepted packet for entity ${subPacket.uuid}! (Game Profile: ${gp.id}) Replacing packets with fake player packets..." }
                                            val textures = npcData.textures
                                            if (textures != null) {
                                                gp.properties.put(
                                                    "textures",
                                                    Property(
                                                        "textures",
                                                        textures.value,
                                                        textures.signature
                                                    )
                                                )
                                            }

                                            // Adds the player to the TAB list (sort of, since the "listed" option is set to false!)
                                            newSubPackets.add(
                                                ClientboundPlayerInfoUpdatePacket(
                                                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                                                    listOf(
                                                        ClientboundPlayerInfoUpdatePacket.Entry(
                                                            gp.id,
                                                            gp,
                                                            false,
                                                            0,
                                                            GameType.DEFAULT_MODE,
                                                            null,
                                                            null
                                                        )
                                                    )
                                                )
                                            )

                                            // Change the spawn type to a player
                                            SparklyNPCManager.setAddEntityPacketSetEntityTypeFieldHandler.invoke(subPacket, EntityType.PLAYER)

                                            // Creates the player itself
                                            newSubPackets.add(subPacket)

                                            // Sets the skin data (to make all skin layers show up)
                                            newSubPackets.add(ClientboundSetEntityDataPacket(subPacket.id, listOf(SynchedEntityData.DataValue(17, EntityDataSerializers.BYTE, 127.toByte()))))

                                            // Set the yaw and pitch because, for some reason, player entities do not have the yaw correctly set when they are spawned
                                            // VERY HACKY because ClientboundRotateHeadPacket does not have an option to just provide the ID
                                            // TODO: Maybe add it in SparklyPaper?
                                            newSubPackets.add(
                                                JVMUnsafeUtils.unsafe.allocateInstance(ClientboundRotateHeadPacket::class.java)
                                                    .let { it as ClientboundRotateHeadPacket }
                                                    .apply {
                                                        SparklyNPCManager.setRotateHeadPacketSetEntityIdFieldHandler.invoke(
                                                            this,
                                                            subPacket.id
                                                        )

                                                        // This is a bit hacky, since we are accessing the entity's current location
                                                        SparklyNPCManager.setRotateHeadPacketSetYHeadRotFieldHandler.invoke(
                                                            this,
                                                            // Yes, the head yaw is controlled by the y rot... I don't know why
                                                            SparklyNPCManager.getAddEntityPacketYRotFieldHandler.invoke(subPacket) as Byte
                                                        )
                                                    }
                                            )

                                            spawnedNPCEntities[subPacket.id] = subPacket.uuid
                                        } else {
                                            newSubPackets.add(subPacket)
                                        }
                                    } else {
                                        newSubPackets.add(subPacket)
                                    }
                                } else {
                                    newSubPackets.add(subPacket)
                                }
                            }

                            super.write(
                                ctx,
                                ClientboundBundlePacket(newSubPackets),
                                promise
                            )
                            return
                        }

                        if (msg is ClientboundRemoveEntitiesPacket) {
                            for (entityId in msg.entityIds.iterator()) {
                                val uuid = spawnedNPCEntities[entityId]

                                if (uuid != null) {
                                    spawnedNPCEntities.remove(entityId)
                                    nmsPlayer.connection.connection.send(ClientboundPlayerInfoRemovePacket(listOf(uuid)))
                                }
                            }
                        }

                        super.write(ctx, msg, promise)
                    }
                }
            )
    }

    @EventHandler
    fun onScoreboardCreation(event: PlayerScoreboardCreatedEvent) {
        // Update currently spawned NPC names
        for ((_, npc) in m.npcEntities) {
            m.updateFakePlayerName(event.phoenixScoreboard.scoreboard, npc)
        }
    }

    @EventHandler(ignoreCancelled = false)
    fun onClick(e: PlayerInteractEntityEvent) {
        m.m.logger.info { "PlayerInteractEntityEvent entity ${e.rightClicked.uniqueId}" }
        val npcData = m.npcEntities[e.rightClicked.uniqueId]
        m.m.logger.info { "Intercepted NPC Data: $npcData" }
        if (npcData != null) {
            e.isCancelled = true
            if (e.hand == EquipmentSlot.HAND) {
                npcData.onRightClickCallback?.invoke(e.player)
            }
        }
    }

    @EventHandler(ignoreCancelled = false)
    fun onClick(e: PrePlayerAttackEntityEvent) {
        m.m.logger.info { "PrePlayerAttackEntityEvent entity ${e.attacked.uniqueId}" }
        val npcData = m.npcEntities[e.attacked.uniqueId]
        m.m.logger.info { "Intercepted NPC Data: $npcData" }
        if (npcData != null) {
            e.isCancelled = true
            npcData.onLeftClickCallback?.invoke(e.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onSpawn(e: CreatureSpawnEvent) {
        if (m.forceSpawn) {
            e.isCancelled = false
        }
    }
}