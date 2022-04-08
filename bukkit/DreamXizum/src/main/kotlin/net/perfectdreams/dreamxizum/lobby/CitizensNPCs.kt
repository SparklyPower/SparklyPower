package net.perfectdreams.dreamxizum.lobby

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.trait.Age
import net.citizensnpcs.trait.Controllable
import net.citizensnpcs.trait.MountTrait
import net.citizensnpcs.trait.SkinTrait
import net.perfectdreams.dreamxizum.config.XizumConfig
import org.bukkit.entity.EntityType
import java.io.File

@Serializable
data class NPCIds(
    var normalNPCId: Int = 0,
    var rankedNPCId: Int = 0,
    var firstSponsorNPCId: Int = 0,
    var secondSponsorNPCId: Int = 0,
    var builderNPCId: Int = 0
)

object CitizensNPCs {
    private val npcModels = with (XizumConfig.models.npcs) { listOf(normal, ranked, sponsor1, sponsor2, builder) }
    lateinit var npcIds: NPCIds

    fun loadFile(file: File) {
        if (!file.exists()) {
            npcIds = NPCIds()
            npcModels.forEach {
                val isBuilder = it == XizumConfig.models.npcs.builder
                val isSponsor = with (XizumConfig.models.npcs) { it == sponsor1 || it == sponsor2 }
                var vehicleNPC: NPC? = null
                var vehicleType = if (isBuilder) EntityType.TURTLE else EntityType.BOAT

                if (isBuilder || isSponsor)
                    vehicleNPC = CitizensAPI.getNPCRegistry().createNPC(vehicleType, ".").apply {
                        getOrAddTrait(Controllable::class.java).isEnabled = true
                        data().setPersistent(NPC.NAMEPLATE_VISIBLE_METADATA, "false")
                        getOrAddTrait(Age::class.java).age = -24000
                        spawn(it.coordinates.toBukkitLocation())
                    }

                val name = when {
                    isBuilder -> "§x§f§2§7§a§d§5§l[Construtora]"
                    isSponsor -> "§e§l[Patrocinador]"
                    else -> it.displayName
                }

                CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name).apply {
                    getOrAddTrait(SkinTrait::class.java).apply {
                        setSkinPersistent("", it.skin.signature, it.skin.texture)
                        setFetchDefaultSkin(false)
                    }
                    spawn(it.coordinates.toBukkitLocation())
                    vehicleNPC?.let { vehicle -> getOrAddTrait(MountTrait::class.java).mountedOn = vehicle.uniqueId }

                    with (XizumConfig.models.npcs) {
                        when (it) {
                            normal -> npcIds.normalNPCId = id
                            ranked -> npcIds.rankedNPCId = id
                            sponsor1 -> npcIds.firstSponsorNPCId = id
                            sponsor2 -> npcIds.secondSponsorNPCId = id
                            builder -> npcIds.builderNPCId = id
                        }
                    }

                }
            }
            file.writeText(Json.encodeToString(npcIds), Charsets.UTF_8)
        } else npcIds = Json.decodeFromString(file.readText())
    }
}