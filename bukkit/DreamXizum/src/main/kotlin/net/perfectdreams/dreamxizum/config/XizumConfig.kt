package net.perfectdreams.dreamxizum.config

import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.config.XizumConfig.positions
import net.perfectdreams.dreamxizum.config.XizumConfig.xizumWorld
import org.bukkit.Location
import org.bukkit.World
import java.io.File
import kotlin.text.Charsets

object XizumConfig {
    lateinit var xizumWorld: World
    lateinit var positions: List<List<CoordinatesModel>>
    lateinit var models: XizumModel

    fun loadFile(file: File) {
        models = DreamUtils.gson.fromJson(file.readText(Charsets.UTF_8), XizumModel::class.java)
        xizumWorld = DreamXizum.INSTANCE.server.getWorld(models.world)!!
        with (models.locations.arenas) {
            positions = listOf(
                with(x1) { listOf(pos1, pos2) },
                with(x2) { listOf(pos1, pos2, pos3, pos4) },
                with(x3) { listOf(pos1, pos2, pos3, pos4, pos5) }
            )
        }
    }
}

class XizumModel {
    lateinit var world: String
    lateinit var locations: LocationsModel
    lateinit var npcs: NPCsModel
    lateinit var takenSkins: TakenSkinsModel
    lateinit var clocks: ClocksModel
}

class CoordinatesModel {
    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    var yaw: Float = 0.0f
    var pitch: Float = 0.0f
    fun toBukkitLocation() = Location(xizumWorld, x, y, z, yaw, pitch)
}

class LocationsModel {
    lateinit var lobby: CoordinatesModel
    lateinit var menu: CoordinatesModel
    lateinit var leaderboard: CoordinatesModel
    lateinit var pageHologram: CoordinatesModel
    lateinit var arenas: ArenasModel
}

class ArenasModel {
    lateinit var x1: ArenaModel
    lateinit var x2: ArenaModel
    lateinit var x3: ArenaModel
    lateinit var ranked: ArenaModel

    fun getPosition(arena: Int, index: Int) = positions[arena - 1][index - 1].toBukkitLocation()
    fun getRankedPosition(index: Int) = (if (index == 1) ranked.pos1 else ranked.pos2).toBukkitLocation()
}

class ArenaModel {
    lateinit var pos1: CoordinatesModel
    lateinit var pos2: CoordinatesModel
    lateinit var pos3: CoordinatesModel
    lateinit var pos4: CoordinatesModel
    lateinit var pos5: CoordinatesModel
    lateinit var pos6: CoordinatesModel
    lateinit var referee: CoordinatesModel
}

class NPCsModel {
    lateinit var normal: NPCModel
    lateinit var ranked: NPCModel
    lateinit var sponsor1: NPCModel
    lateinit var sponsor2: NPCModel
    lateinit var builder: NPCModel

    lateinit var create: NPCModel
    lateinit var random: NPCModel

    lateinit var x1: NPCModel
    lateinit var x2: NPCModel
    lateinit var x3: NPCModel

    lateinit var custom: NPCModel
    lateinit var kits: NPCModel
    lateinit var items: NPCModel

    lateinit var kit1: NPCModel
    lateinit var kit2: NPCModel
    lateinit var kit3: NPCModel
    lateinit var kit4: NPCModel

    lateinit var playerKit: NPCModel
    lateinit var pluginKit: NPCModel

    lateinit var pluginKit1: NPCModel
    lateinit var pluginKit2: NPCModel
    lateinit var pluginKit3: NPCModel

    lateinit var barehands: NPCModel
    lateinit var swords: NPCModel
    lateinit var axes: NPCModel

    lateinit var swordsLoritta: NPCModel
    lateinit var swordsPantufa: NPCModel
    lateinit var swordsGabriela: NPCModel

    lateinit var axesLoritta: NPCModel
    lateinit var axesPantufa: NPCModel
    lateinit var axesGabriela: NPCModel

    lateinit var duckLoritta: NPCModel
    lateinit var frogPantufa: NPCModel
    lateinit var foxGabriela: NPCModel

    lateinit var armorLoritta: NPCModel
    lateinit var armorPantufa: NPCModel

    lateinit var ironArmor: NPCModel
    lateinit var diamondArmor: NPCModel
    lateinit var netheriteArmor: NPCModel

    lateinit var protectionArmor: NPCModel
    lateinit var thornsArmor: NPCModel
    lateinit var unbreakableArmor: NPCModel

    lateinit var bow: NPCModel
    lateinit var crossbow: NPCModel
    lateinit var trident: NPCModel
    lateinit var shield: NPCModel

    lateinit var bowPower: NPCModel
    lateinit var bowPunch: NPCModel
    lateinit var bowFlame: NPCModel
    lateinit var bowInfinity: NPCModel

    lateinit var crossbowPiercing: NPCModel
    lateinit var crossbowQuickCharge: NPCModel
    lateinit var crossbowMultishot: NPCModel

    lateinit var tridentLoyalty: NPCModel

    lateinit var shieldUnbreakable: NPCModel

    lateinit var betSonecas: NPCModel
    lateinit var betCash: NPCModel

    lateinit var allowMcMMO: NPCModel
    lateinit var dropHeads: NPCModel
    lateinit var battleTime: NPCModel
    lateinit var pvpVersion: NPCModel

    lateinit var remove10k: NPCModel
    lateinit var remove100k: NPCModel
    lateinit var add10k: NPCModel
    lateinit var add100k: NPCModel

    lateinit var remove10: NPCModel
    lateinit var remove100: NPCModel
    lateinit var add10: NPCModel
    lateinit var add100: NPCModel

    lateinit var undo: NPCModel
    lateinit var save: NPCModel
    lateinit var forward: NPCModel
    lateinit var finish: NPCModel

    lateinit var referee: NPCModel
}

open class NPCModel {
    open lateinit var displayName: String
    open lateinit var coordinates: CoordinatesModel
    open lateinit var skin: SkinModel
}

class SkinModel {
    lateinit var texture: String
    lateinit var signature: String
}

class TakenSkinsModel {
    lateinit var kit1: SkinModel
    lateinit var kit2: SkinModel
    lateinit var kit3: SkinModel
    lateinit var kit4: SkinModel
}

class ClocksModel {
    lateinit var threePM: SkinModel
    lateinit var fivePM: SkinModel
    lateinit var sevenPM: SkinModel
    lateinit var tenPM: SkinModel
}