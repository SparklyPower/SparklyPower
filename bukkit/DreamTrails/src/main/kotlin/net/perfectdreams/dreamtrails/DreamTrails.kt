package net.perfectdreams.dreamtrails

import com.github.salomonbrys.kotson.fromJson
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.DreamUtils.gson
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamtrails.commands.TrailsCommand
import net.perfectdreams.dreamtrails.listeners.MoveListener
import net.perfectdreams.dreamtrails.utils.ColoredArmorData
import net.perfectdreams.dreamtrails.utils.Halo
import net.perfectdreams.dreamtrails.utils.PlayerTrailsData
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.util.*

class DreamTrails : KotlinPlugin() {
	companion object {
		const val USE_TRAILS_PERMISSION = "dreamtrails.use"
		const val USE_FASHION_ARMOR_PERMISSION = "dreamtrails.fashion"
		const val USE_HALO_PERMISSION = "dreamtrails.halo"
		const val USE_MOVE_TRAIL_PERMISSION = "dreamtrails.move"
		val IS_FANCY_LEATHER_ARMOR_KEY = SparklyNamespacedBooleanKey("is_fancy_leather_armor_key")
	}

	val coloredArmorData = ColoredArmorData(
		ColoredArmorData.ArmorColor(0, true),
		ColoredArmorData.ArmorColor(127, true),
		ColoredArmorData.ArmorColor(255, true)
	)
	val coloredHaloData = ColoredArmorData(
		ColoredArmorData.ArmorColor(0, true),
		ColoredArmorData.ArmorColor(127, true),
		ColoredArmorData.ArmorColor(255, true)
	)

	var playerTrails = HashMap<UUID, PlayerTrailsData>()

	fun applyColoredLeatherEffectIfNeeded(player: Player, itemStack: ItemStack?, color: Color) {
		if (itemStack == null)
			return

		if (itemStack.hasItemMeta())
			return

		val type = itemStack.type

		if (type != Material.LEATHER_HELMET && type != Material.LEATHER_CHESTPLATE && type != Material.LEATHER_LEGGINGS && type != Material.LEATHER_BOOTS)
			return

		if (!itemStack.itemMeta.persistentDataContainer.get(IS_FANCY_LEATHER_ARMOR_KEY))
			return

		itemStack.meta<LeatherArmorMeta> {
			this.setColor(color)
		}
	}

	override fun softEnable() {
		super.softEnable()

		registerEvents(MoveListener(this))
		registerCommand(TrailsCommand(this))

		dataFolder.mkdirs()
		val playerTrailsFile = File(dataFolder, "player_trails.json")

		if (playerTrailsFile.exists()) {
			playerTrails = gson.fromJson(
				playerTrailsFile.readText()
			)
		}

		launchMainThread {
			while (true) {
				val r = coloredArmorData.r.addAndGet()
				val g = coloredArmorData.g.addAndGet()
				val b = coloredArmorData.b.addAndGet()

				val color = Color.fromRGB(r, g, b)

				Bukkit.getOnlinePlayers().filter { it.hasPermission(DreamTrails.USE_FASHION_ARMOR_PERMISSION) }.forEach {
					val helmet = it.inventory.helmet
					val chestplate = it.inventory.chestplate
					val leggings = it.inventory.leggings
					val boots = it.inventory.boots

					applyColoredLeatherEffectIfNeeded(it, helmet, color)
					applyColoredLeatherEffectIfNeeded(it, chestplate, color)
					applyColoredLeatherEffectIfNeeded(it, leggings, color)
					applyColoredLeatherEffectIfNeeded(it, boots, color)
				}

				delayTicks(5L)
			}
		}

		launchMainThread {
			while (true) {
				val r = coloredHaloData.r.addAndGet()
				val g = coloredHaloData.g.addAndGet()
				val b = coloredHaloData.b.addAndGet()
				val haloColor = Color.fromRGB(r, g, b)

				Bukkit.getOnlinePlayers().filter { !it.hasPermission(USE_TRAILS_PERMISSION) }.forEach {
					playerTrails.remove(it.uniqueId)
				}

				for (player in Bukkit.getOnlinePlayers()) {
					if (!hasParticlesEnabled(player))
						continue

					if (player.hasPermission(USE_TRAILS_PERMISSION)) {
						val trailData = playerTrails[player.uniqueId] ?: continue
						val activeHalo = trailData.activeHalo
						if (activeHalo != null) {
							for (it in 0 until 360 step 4) {
								val asRadians = Math.toRadians(it.toDouble())
								val sin = Math.sin(asRadians)
								val cos = Math.cos(asRadians)

								val particleLocation = player.location.add(
									sin * 0.3,
									2.0,
									cos * 0.3
								)

								val color = if (activeHalo == Halo.RAINBOW)
									haloColor
								else
									activeHalo.color

								val dustOptions = Particle.DustOptions(color, 0.3f)
								player.world.spawnParticle(
									Particle.DUST,
									particleLocation,
									1,
									0.0,
									0.0,
									0.0,
									dustOptions
								)
							}
						}
					} else {
						playerTrails.remove(player.uniqueId)
					}
				}
				delayTicks(3L)
			}
		}
	}

	fun hasParticlesEnabled(player: Player) =
		player.location.world.name !in listOf("Quiz", "Labirinto", "EventoFight", "Corrida", "MinaRecheada", "TorreDaMorte", "ArenasPvP")
				&& !DreamVanishAPI.isVanishedOrInvisible(player)

	override fun softDisable() {
		super.softDisable()

		val playerTrailsFile = File(dataFolder, "player_trails.json")
		playerTrailsFile.writeText(gson.toJson(playerTrails))
	}
}