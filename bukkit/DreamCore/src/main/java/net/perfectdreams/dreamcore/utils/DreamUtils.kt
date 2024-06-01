package net.perfectdreams.dreamcore.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import net.md_5.bungee.api.chat.BaseComponent
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.scheduler.BukkitScheduler
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
// import protocolsupport.api.ProtocolSupportAPI
// import protocolsupport.api.ProtocolVersion
import java.util.*
import java.util.logging.Level

/**
 * Extensões gerais e variáveis utilitárias
 */
object DreamUtils {
	@JvmStatic
	val random = SplittableRandom()
	@JvmStatic
	val SLOW_RANDOM = Random()
	@JvmStatic
	val gson: Gson
	@JvmStatic
	val jsonParser = JsonParser()
	val nmsVersion: String by lazy { Bukkit.getServer()::class.java.getPackage().name.split("\\.")[3] }
	const val HEADER_LINE = "§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-";
	val http = HttpClient(CIO) {
		expectSuccess = false
	}

	init {
		val gsonBuilder = GsonBuilder()
			.registerTypeAdapter<Location> {
				serialize {
					val jsonObject = JsonObject()
					jsonObject["x"] = it.src.x
					jsonObject["y"] = it.src.y
					jsonObject["z"] = it.src.z
					jsonObject["yaw"] = it.src.yaw
					jsonObject["pitch"] = it.src.pitch
					jsonObject["world"] = it.src.world.name
					return@serialize jsonObject
				}

				deserialize {
					val x = it.json["x"].double
					val y = it.json["y"].double
					val z = it.json["z"].double
					val yaw = it.json["yaw"].float
					val pitch = it.json["pitch"].float
					val worldName = it.json["world"].string

					Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)
				}
			}
			.registerTypeAdapter<ItemStack> {
				serialize {
					val jsonObject = JsonObject()
					jsonObject["type"] = it.src.type.name
					jsonObject["amount"] = it.src.amount

					val enchantmentMap = JsonObject()
					for ((enchantment, level) in it.src.enchantments) {
						enchantmentMap[enchantment.name] = level
					}
					jsonObject["enchantments"] = enchantmentMap

					if (it.src.hasItemMeta()) {
						val jsonMeta = JsonObject()
						val meta = it.src.itemMeta
						jsonMeta["displayName"] = meta.displayName
						jsonMeta["lore"] = it.context.serialize(meta.lore)

						jsonMeta["isUnbreakable"] = meta.isUnbreakable
						jsonMeta["itemFlags"] = it.context.serialize(meta.itemFlags)

						if (meta is Damageable) {
							jsonMeta["damage"] = meta.damage
						}

						if (meta is MapMeta) {
							val mapMeta = JsonObject()
							mapMeta["mapId"] = meta.mapId
							jsonMeta["map"] = mapMeta
						}

						if (meta is LeatherArmorMeta) {
							val leatherMeta = JsonObject()
							leatherMeta["r"] = meta.color.red
							leatherMeta["g"] = meta.color.green
							leatherMeta["b"] = meta.color.blue

							jsonMeta["color"] = leatherMeta
						}

						jsonObject["itemMeta"] = jsonMeta
					}

					return@serialize jsonObject
				}

				deserialize {
					val jsonObject = it.json.obj
					val type = Material.valueOf(jsonObject["type"].string)
					val amount = jsonObject["amount"].nullInt ?: 0
					val enchantments = jsonObject["enchantments"].nullObj
					val jsonMeta = jsonObject["itemMeta"].nullObj
					val attributes = jsonObject["attributes"].nullObj
					val persistentDataContainer = jsonObject["persistentDataContainer"].nullObj

					var itemStack = ItemStack(type, amount)

					if (enchantments != null) {
						for ((enchantmentName, level) in it.context.deserialize<Map<String, Int>>(enchantments)) {
							itemStack.addUnsafeEnchantment(Enchantment.getByName(enchantmentName)!!, level)
						}
					}

					if (jsonMeta != null) {
						val meta = itemStack.itemMeta
						val displayName = jsonMeta["displayName"].nullString
						if (displayName != null)
							meta.setDisplayName(displayName)

						val damage = jsonObject["damage"].nullInt

						if (damage != null) {
							meta as Damageable
							meta.damage = damage
						}

						if (jsonMeta.has("lore")) {
							meta.lore = it.context.deserialize<List<String>>(jsonMeta["lore"])
						}

						meta.isUnbreakable = jsonMeta["isUnbreakable"].nullBool ?: false

						if (jsonMeta.has("itemFlags")) {
							for (itemFlagName in it.context.deserialize<List<String>>(jsonMeta["itemFlags"])) {
								meta.addItemFlags(ItemFlag.valueOf(itemFlagName))
							}
						}

						if (jsonMeta.has("color")) {
							val leatherMeta = jsonMeta["color"].obj

							meta as LeatherArmorMeta
							meta.setColor(Color.fromRGB(leatherMeta["r"].int, leatherMeta["g"].int, leatherMeta["b"].int))
						}

						if (jsonMeta.has("map")) {
							val mapMeta = jsonMeta["map"].obj

							meta as MapMeta
							val mapId = mapMeta["mapId"].nullInt
							if (mapId != null)
								meta.mapId = mapId
						}

						itemStack.itemMeta = meta
					}

					// TODO - 1.20.6: Fix this!
					//  (we don't really need to fix this, but we need to check if all of our serialized items are using PDC)
					// if (attributes != null) {
					// 	for ((key, element) in attributes.entrySet()) {
					// 		itemStack = itemStack.storeMetadata(key, element.string)
					// 	}
					// }

					if (persistentDataContainer != null) {
						itemStack.meta<ItemMeta> {
							for ((key, element) in persistentDataContainer.entrySet()) {
								val dataTypeAsString = element["type"].string

								when (dataTypeAsString) {
									"BYTE" -> {
										this.persistentDataContainer.set(
											SparklyNamespacedKey(key),
											PersistentDataType.BYTE,
											element["value"].byte
										)
									}
									"STRING" -> {
										this.persistentDataContainer.set(
											SparklyNamespacedKey(key),
											PersistentDataType.STRING,
											element["value"].string
										)
									}
									"INTEGER" -> {
										this.persistentDataContainer.set(
											SparklyNamespacedKey(key),
											PersistentDataType.INTEGER,
											element["value"].int
										)
									}
									"LONG" -> {
										this.persistentDataContainer.set(
											SparklyNamespacedKey(key),
											PersistentDataType.LONG,
											element["value"].long
										)
									}
									"SHORT" -> {
										this.persistentDataContainer.set(
											SparklyNamespacedKey(key),
											PersistentDataType.SHORT,
											element["value"].short
										)
									}
									"DOUBLE" -> {
										this.persistentDataContainer.set(
											SparklyNamespacedKey(key),
											PersistentDataType.DOUBLE,
											element["value"].double
										)
									}
									"FLOAT" -> {
										this.persistentDataContainer.set(
											SparklyNamespacedKey(key),
											PersistentDataType.FLOAT,
											element["value"].float
										)
									}
									else -> throw IllegalArgumentException("Unsupported conversion from $dataTypeAsString to PersistentDataType")
								}
							}
						}
					}
					return@deserialize itemStack
				}
			}
		gson = gsonBuilder.create()
	}

	/**
	 * Retrieves the user info for the specified [uuid]
	 *
	 * @param uuid the user's unique ID
	 * @return the user's data, if present
	 */
	fun retrieveUserInfo(uuid: UUID): User? {
		assertAsyncThread(true)
		return transaction(Databases.databaseNetwork) {
			User.findById(uuid)
		}
	}

	/**
	 * Retrieves the user info for the specified [playerName]
	 *
	 * @param playerName user's name
	 * @return the user's data, if present
	 */
	fun retrieveUserInfo(playerName: String): User? {
		assertAsyncThread(true)
		return transaction(Databases.databaseNetwork) {
			User.find { Users.username eq playerName }
				.firstOrNull()
		}
	}

	/**
	 * Retrieves the user info for the specified [playerName], ignoring capitalization
	 *
	 * @param playerName user's name
	 * @return the user's data, if present
	 */
	fun retrieveUserInfoCaseInsensitive(playerName: String): User? {
		assertAsyncThread(true)
		return transaction(Databases.databaseNetwork) {
			User.find { Users.username.lowerCase() eq playerName.lowercase() }
				.firstOrNull()
		}
	}

	/**
	 * Retrieves the player's UUID for the specified [playerName]
	 * If the player does not exist on the database, it will default
	 * to "OfflinePlayer:$playerName"
	 *
	 * @param playerName user's name
	 * @return the user's data, if present
	 */
	fun retrieveUserUniqueId(playerName: String): UUID {
		assertAsyncThread(true)
		return retrieveUserInfo(playerName)?.id?.value ?: UUID.nameUUIDFromBytes("OfflinePlayer:$playerName".toByteArray(Charsets.UTF_8))
	}

	/**
	 * Verifica se o código está sendo executado na thread principal do servidor, se sim, um UnsupportedOperationException será criado
	 *
	 * @param fatal (opcional) se o exception deverá ficar entre um try ... catch
	 */
	fun assertAsyncThread(fatal: Boolean = false) {
		if (Bukkit.isPrimaryThread()) {
			if (fatal) {
				throw UnsupportedOperationException("Async operation in main thread!")
			} else {
				try {
					throw UnsupportedOperationException("Async operation in main thread!")
				} catch (e: UnsupportedOperationException) {
					e.printStackTrace()
				}
			}
		}
	}

	/**
	 * Verifica se o código está sendo executado na thread principal do servidor, se não, um UnsupportedOperationException será criado
	 *
	 * @param fatal (opcional) se o exception deverá ficar entre um try ... catch
	 */
	fun assertMainThread(fatal: Boolean = false) {
		if (!Bukkit.isPrimaryThread()) {
			if (fatal) {
				throw UnsupportedOperationException("Asynchronous access is unsupported!")
			} else {
				try {
					throw UnsupportedOperationException("Asynchronous access is unsupported!")
				} catch (e: UnsupportedOperationException) {
					e.printStackTrace()
				}
			}
		}
	}
}

fun broadcast(message: String, permission: String = Server.BROADCAST_CHANNEL_USERS): Int {
	return Bukkit.broadcast(message, permission)
}

fun broadcast(baseComponent: BaseComponent) {
	Bukkit.spigot().broadcast(baseComponent)
}

fun Plugin.registerEvents(listener: Listener) {
	Bukkit.getPluginManager().registerEvents(listener, this)
}

fun <T> List<T>.getRandom(): T {
	return this[DreamUtils.random.nextInt(this.size)]
}

fun scheduler(): BukkitScheduler {
	return Bukkit.getScheduler()
}

fun server(): Server {
	return Bukkit.getServer()
}

fun onlinePlayers(): Collection<Player> {
	return Bukkit.getOnlinePlayers()
}

fun createInventory(holder: InventoryHolder? = null, size: Int = 9, name: String? = null): Inventory {
	return Bukkit.createInventory(holder, size, name ?: "")
}

fun createBossBar(title: String, color: BarColor, style: BarStyle, vararg flags: BarFlag): BossBar {
	return Bukkit.getServer().createBossBar(title, color, style, *flags)
}

fun pluginManager(): PluginManager {
	return Bukkit.getServer().pluginManager
}

val isPrimaryThread: Boolean
	get() = Bukkit.getServer().isPrimaryThread

val withoutPermission: String
	get() = DreamCore.dreamConfig.strings.withoutPermission

val serverName: String
	get() = DreamCore.dreamConfig.serverName

val bungeeName: String
	get() = DreamCore.dreamConfig.bungeeName

val World.blacklistedTeleport: Boolean
	get() {
		return DreamCore.dreamConfig.blacklistedWorldsTeleport.contains(this.name)
	}

val isStaffPermission: String
	get() = DreamCore.dreamConfig.strings.staffPermission

val Location.blacklistedTeleport: Boolean
	get() {
		if (DreamCore.dreamConfig.blacklistedWorldsTeleport.contains(this.world.name)) {
			return true
		}
		val regions = WorldGuardUtils.getRegionIdsAt(this)
		return regions.any { DreamCore.dreamConfig.blacklistedRegionsTeleport.contains(it) }
	}

fun String.toPlayerExact(): Player? {
	return Bukkit.getPlayerExact(this)
}

fun generateCommandInfo(command: String, arguments: Map<String, String> = mapOf(), tips: List<String> = listOf()): String {
	var base = "§eComo usar: §6/$command"

	for ((key, _) in arguments) {
		base += " " + key
	}

	base += "\n"
	for ((argument, info) in arguments) {
		base += "§8★ §f$argument §8- §7$info\n"
	}

	base += "§f\n"
	if (tips.isNotEmpty()) {
		for ((index, tip) in tips.withIndex()) {
			base += "\n"
			if (index == tips.size - 1)
				base += "§8• §7$tip"
		}
	}
	return base
}