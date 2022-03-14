package net.perfectdreams.dreamcore.utils

import com.comphenix.packetwrapper.*
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.*
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import com.mojang.datafixers.util.Pair
import com.okkero.skedule.schedule
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Team
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DreamNPC(val name: String, val location: Location, val equipment: Set<ItemStack>, val callback: ((Player) -> Unit)?, private val signedProperty: WrappedSignedProperty?) {
    companion object {
        private val byteSerializer = WrappedDataWatcher.Registry.get(java.lang.Byte::class.java)
        private val poseSerializer = WrappedDataWatcher.Registry.get(EnumWrappers.getEntityPoseClass())
        private val blockSerializer = WrappedDataWatcher.Registry.getBlockPositionSerializer(true)

        private val nmsPose = EnumWrappers.EntityPose.SLEEPING.toNms()

        private val watchableObject = WrappedDataWatcher.WrappedDataWatcherObject(17, byteSerializer)
        private val watchableRightClick = WrappedDataWatcher.WrappedDataWatcherObject(8, byteSerializer)
        private val watchablePose = WrappedDataWatcher.WrappedDataWatcherObject(6, poseSerializer)
        private val watchableBed = WrappedDataWatcher.WrappedDataWatcherObject(14, blockSerializer)

        private const val EMPTY_NAME = "§a§a§a§a§a§a"
        private val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        init {
            (scoreboard.getTeam("dreamNPC") ?: scoreboard.registerNewTeam("dreamNPC")).apply {
                setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
                addEntry(EMPTY_NAME)
            }
        }

        private fun splitName(name: String) = name.split("\n")
        private fun isValidName(name: String) = splitName(name).size == 1 && name.length <= 16 && name.isNotBlank()
        private fun getValidName(name: String) = if (isValidName(name)) name else EMPTY_NAME

        private val helmets = getAllArmorParts("HELMET")
        private val chestplates = getAllArmorParts("CHESTPLATE")
        private val leggings = getAllArmorParts("LEGGINGS")
        private val boots = getAllArmorParts("BOOTS")

        private fun getAllArmorParts(part: String) = setOf("LEATHER", "CHAINMAIL", "IRON", "GOLDEN", "DIAMOND", "NETHERITE")
            .map { Material.getMaterial("${it}_$part") }

        private fun inferSlot(material: Material) = when (material) {
            in helmets -> EquipmentSlot.HEAD
            in chestplates -> EquipmentSlot.CHEST
            in leggings -> EquipmentSlot.LEGS
            in boots -> EquipmentSlot.FEET
            else -> EquipmentSlot.MAINHAND
        }

        private val callbacks = mutableMapOf<Int, ((Player) -> Unit)>().also {
            val cooldown = WeakHashMap<Player, Long>()
            ProtocolLibrary.getProtocolManager().addPacketListener(
                object : PacketAdapter(DreamCore.INSTANCE, ListenerPriority.HIGHEST, PacketType.Play.Client.USE_ENTITY) {
                    override fun onPacketReceiving(event: PacketEvent) {
                        val player = event.player
                        // The event fires four times on right click, so we add a little delay
                        cooldown[player]?.let { ms -> if (System.currentTimeMillis() - ms < 50) return }
                        it[event.packet.integers.read(0)]?.let {
                            DreamCore.INSTANCE.launchMainThread {
                                it.invoke(player)
                            }
                        }
                        cooldown[player] = System.currentTimeMillis()
                    }
                }
            )
        }
    }

    private val ID = Entity.nextEntityId()
    private val UUID = java.util.UUID.randomUUID()
    private val gameProfile = WrappedGameProfile(UUID, getValidName(name))
    private val wrappedName = WrappedChatComponent.fromText(getValidName(name))

    private val viewers: MutableSet<Player> = Collections.newSetFromMap(ConcurrentHashMap())
    private var hologram: WrapperHologram? = null

    init {
        val lines = splitName(name)
        if (!isValidName(name)) {
            val hologramLocation = location.clone()
            hologramLocation.y += -.175 + (lines.size - 1) * .285
            hologram = WrapperHologram(hologramLocation, lines.toMutableList())
        }

        signedProperty?.let {
            gameProfile.properties.removeAll("textures")
            gameProfile.properties.put("textures", it)
        }
        callback?.let { callbacks[ID] = it }
    }

    var isSleeping = false
        set (value) {
            field = value
            if (value) {
                dataWatcher.setObject(watchablePose, nmsPose)
                dataWatcher.setObject(watchableBed, Optional.of(BlockPosition.getConverter().getGeneric(BlockPosition(location.toVector()))))
                updateMetadata()
            }
        }

    var isRightClicking = false
        set (value) {
            field = value
            dataWatcher.setObject(watchableRightClick, (if (value) 1 else 0).toByte())
            updateMetadata()
        }

    var shouldSpawnPunching = false

    private val spawnEntityPacket = WrapperPlayServerNamedEntitySpawn().apply {
        entityID = ID
        playerUUID = UUID
        x = location.x
        y = location.y
        z = location.z
        yaw = location.yaw
        pitch = location.pitch
    }

    private val destroyEntityPacket = ClientboundRemoveEntitiesPacket(ID)

    private val dataWatcher = WrappedDataWatcher()
    private var infoData = PlayerInfoData(gameProfile, 1, EnumWrappers.NativeGameMode.SURVIVAL, wrappedName)

    private val addPlayerPacket = WrapperPlayServerPlayerInfo().apply {
        action = EnumWrappers.PlayerInfoAction.ADD_PLAYER
        data = listOf(infoData)
    }

    private val removePlayerPacket = WrapperPlayServerPlayerInfo().apply {
        action = EnumWrappers.PlayerInfoAction.REMOVE_PLAYER
        data = listOf(infoData)
    }

    private val metadataPacket = WrapperPlayServerEntityMetadata().apply {
        dataWatcher.setObject(watchableObject, 126.toByte())
        entityID = ID
    }

    private val itemPairs = mutableListOf<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>>().apply {
        equipment.forEach {
            add(Pair(
                inferSlot(it.type),
                net.minecraft.world.item.ItemStack.fromBukkitCopy(it)
            ))
        }
    }
    private var equipmentPacket = ClientboundSetEquipmentPacket(ID, itemPairs)

    private val swingArmPacket = WrapperPlayServerAnimation().apply {
        entityID = ID
        animation = 0
    }

    /**
     * For some reason, the NPC always spawns facing south, regardless of the yaw set in the packet.
     * To solve this, we need to send a head rotation packet with the yaw value.
     * Sometimes, though, the body will not match the head's angle. To fix that, we either add or
     * subtract 45 from the yaw, and then send a teleport packet with this new yaw value. It's also
     * possible to fix it by having the NPC punch the air.
     */
    private val headRotationPacket = WrapperPlayServerEntityHeadRotation().apply {
        headYaw = (location.yaw * 256.0f / 360.0f).toInt().toByte()
        entityID = ID
    }

    private val teleportPacket = WrapperPlayServerEntityTeleport().apply {
        entityID = ID
        x = location.x
        y = location.y
        z = location.z
        yaw = location.yaw + (45.0f * if (location.yaw <= 0) -1 else 1)
        pitch = location.pitch
    }

    private fun updateMetadata() {
        metadataPacket.metadata = dataWatcher.watchableObjects
        viewers.forEach { metadataPacket.sendPacket(it) }
    }

    fun updateSkin(texture: String, signature: String) {
        val signedProperty = WrappedSignedProperty("textures", texture, signature)
        with (gameProfile) {
            properties.removeAll("textures")
            properties.put("textures", signedProperty)
        }

        infoData = PlayerInfoData(gameProfile, 1, EnumWrappers.NativeGameMode.SURVIVAL, wrappedName)

        viewers.forEach {
            (it as CraftPlayer).handle.connection.send(destroyEntityPacket)
            addViewer(it, false)
        }
    }

    fun changeItem(material: Material, enchanted: Boolean = false, slot: EquipmentSlot = inferSlot(material)) =
        changeItem(material.toItemStack().apply {
            if (enchanted) addEnchantment(Enchantment.MENDING, 1)
        }, slot)

    fun changeItem(itemStack: ItemStack, slot: EquipmentSlot = inferSlot(itemStack.type)) {
        val item = net.minecraft.world.item.ItemStack.fromBukkitCopy(itemStack)
        val index = itemPairs.indexOfFirst { it.first == slot }
        val pair = Pair(slot, item)
        if (index == -1) itemPairs.add(pair) else itemPairs[index] = pair
        equipmentPacket = ClientboundSetEquipmentPacket(ID, itemPairs)
        viewers.forEach { (it as CraftPlayer).handle.connection.send(equipmentPacket) }
    }

    /**
     * Updates the specified line only if the NPC has a multilined name
     */
    @Throws(NullPointerException::class)
    fun updateLine(index: Int, line: String) = hologram!!.setLine(index, line)

    /**
     * Returns the lines of the name only if the NPC has a multilined name
     */
    @Throws(NullPointerException::class)
    fun getLines() = hologram!!.lines

    fun punch() = viewers.forEach { swingArmPacket.sendPacket(it) }

    fun addViewer(viewer: Player, shouldSpawnHologram: Boolean = true) {
        if (shouldSpawnHologram) hologram?.addViewer(viewer)
        viewers.add(viewer)

        addPlayerPacket.sendPacket(viewer)
        spawnEntityPacket.sendPacket(viewer)
        updateMetadata()
        if (itemPairs.isNotEmpty()) (viewer as CraftPlayer).handle.connection.send(equipmentPacket)

        headRotationPacket.sendPacket(viewer)
        if (shouldSpawnPunching) punch() else teleportPacket.sendPacket(viewer)

        /**
         * We have to wait before sending the packet with the REMOVE_PLAYER action,
         * the skin won't load otherwise.
         */
        scheduler().schedule(DreamCore.INSTANCE) {
            waitFor(10L)
            removePlayerPacket.sendPacket(viewer)
        }
    }

    fun removeViewer(viewer: Player) {
        viewers.remove(viewer)
        (viewer as CraftPlayer).handle.connection.send(destroyEntityPacket)
        hologram?.removeViewer(viewer)
    }

    fun destroy() {
        callbacks.remove(ID)
        viewers.forEach { removeViewer(it) }
    }
}

fun createNPC(name: String, location: Location, block: DreamNPCBuilder.() -> Unit) = DreamNPCBuilder(name, location).apply(block).build()

class DreamNPCBuilder(val name: String, val location: Location) {
    private val equipment = mutableSetOf<ItemStack>()
    private var onClick: ((Player) -> Unit)? = null
    private var properties: WrappedSignedProperty? = null

    fun equip(material: Material, enchanted: Boolean = false) {
        val item = ItemStack(material, 1)
        if (enchanted) item.addEnchantment(Enchantment.MENDING, 1)
        equipment.add(item)
    }

    fun onClick(callback: (Player) -> Unit) {
        onClick = callback
    }

    fun skin(block: DreamNPCSkinBuilder.() -> Unit) {
        properties = DreamNPCSkinBuilder().apply(block).build()
    }

    fun build() = DreamNPC(name, location, equipment, onClick, properties)
}

class DreamNPCSkinBuilder {
    lateinit var texture: String
    lateinit var signature: String

    fun build() = WrappedSignedProperty("textures", texture, signature)
}