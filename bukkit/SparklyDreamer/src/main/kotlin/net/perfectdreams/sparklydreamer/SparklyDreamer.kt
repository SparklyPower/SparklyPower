package net.perfectdreams.sparklydreamer

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import kotlinx.coroutines.delay
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldDimensions
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.sparklydreamer.utils.APIServer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R2.CraftServer
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Husk
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.sqrt

class SparklyDreamer : KotlinPlugin(), Listener {
	val npcKey = SparklyNamespacedBooleanKey("npc")
	var apiServer = APIServer(this)
	val npcEntities = ConcurrentHashMap<UUID, SparklyNPCData>()
	lateinit var playerNames: Scoreboard
	private val chatColors = ChatColor.values()

	override fun softEnable() {
		super.softEnable()

		playerNames = Bukkit.getScoreboardManager().newScoreboard

		apiServer.start()
		registerCommand(NPCTestCommand())
		registerCommand(ChangeSkinCommand())

		playerNames.registerNewObjective("alphys", "dummy")
		playerNames.getObjective("alphys")!!.displaySlot = DisplaySlot.SIDEBAR

		registerEvents(this)
	}

	override fun softDisable() {
		super.softDisable()

		apiServer.stop()
	}

	inner class ChangeSkinCommand : SparklyCommandDeclarationWrapper {
		override fun declaration() = sparklyCommand(listOf("changeskin")) {
			executor = ChangeSkinExecutor()
		}

		inner class ChangeSkinExecutor : SparklyCommandExecutor() {
			override fun execute(context: CommandContext, args: CommandArguments) {
				val player = context.requirePlayer()
				player.sendMessage("Updating skin...")

				// This works, and it is WAY simpler than the whatever hacky hack SkinsRestorer is doing
				val playerProfile = player.playerProfile
				playerProfile.setProperty(
					ProfileProperty(
						"textures",
						"ewogICJ0aW1lc3RhbXAiIDogMTY5ODI2NjU0ODk4MSwKICAicHJvZmlsZUlkIiA6ICJiODM3ZjNkNjc3MjA0YjQ2OWE4ZTJiNmJmMDRhMjQxYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYW50dWZpbmhhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlkZDEyNTZkNmI5MjNkM2FlNjE4ZjVmMTNmZGEwYTY0MGY2ZTQ1ODVhZTNjMzUzZDg1ZTlmZTY1ODk5NmM4YWEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
						"UzRNP1iGvhcKiDCnYu0weJ1xl2sIOZz/vPlWN+KBZuIM2Jiqv+6n/ozds0wNUf8et7v8tN3Nh3YLj54TrJez5xTtOros7HSjFBlf8+HYFrR/0IqseCksvxC0gSeouuTNMVrYzFI0OyG16ZShlneMQQQ7rgIhP5xUSrgy7Xow4tLV2Uy9Fnpd7+bbKCJxs+PXnWoruJVN1NfSYD//b223v0P1m1rk1/F3nZ+m/Bv1zcgmclCwqOGBkQ90NgKHU6yo2Rfi95jxdYXxv73sF3CSAbmWCoZZZzdRumEWJSK68bDRcD0PVRWNidnpBhfOU/Lvh+pcsnwXNekoGmeu11vXKliafs2LK+BNT19F73eYI0nOlpVlbHrKaNRZgKtL4zrHp8ZsK6koh25QCGvygwwHzg1s3flfRKFBQztg8GlsiMIK6SNBP4HdAd/FNqXFW6p9BycKAR9HODUI9S+Nvgd6DQtIvCbgBCxDx4hqqR+o1451euSF9PPRuHLP1judZ8+bYKnTrXxDA/M4Yq+QW/lbkBY4r+52u+9YKbdFTLE6zPn0LkBnYSXfmib2FI3A7BvVPgN9vHQ03UodS1gSFdvVsS7qmkKP+02gYNpvhXAOW3G+k4oFV7ytHxEWw2SaEl2H0SLsG+lwU3gKXSaStkteu1T28nBauaWq2yQ2MFOoZ0A="
					)
				)
				player.playerProfile = playerProfile

				player.sendMessage("Success!")
			}
		}
	}

	inner class NPCTestCommand : SparklyCommandDeclarationWrapper {
		override fun declaration() = sparklyCommand(listOf("npctest")) {
			executor = NPCTestExecutor()
		}

		inner class NPCTestExecutor : SparklyCommandExecutor() {
			override fun execute(context: CommandContext, args: CommandArguments) {
				if (true) {
					val player = context.requirePlayer()
					val sparklyNPC = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
						this@SparklyDreamer,
						player.location,
						"Yoru",
						skinTextures = SkinTexture(
							"ewogICJ0aW1lc3RhbXAiIDogMTY5ODE4ODE3NzE1NiwKICAicHJvZmlsZUlkIiA6ICIyMGFhNjVjOGM5M2M0MTI3YmIyMWFjMmNkZTZhOTg0NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbGVqb2RpemMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNhZmM0MjlmOTI1YjExMDkwYWU1OGRiNmE4M2VkNzk2MmQ5YzdiMjNhZmM3N2IzOGQ4Y2RmYzI3NzcxMGEwYyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIzNDBjMGUwM2RkMjRhMTFiMTVhOGIzM2MyYTdlOWUzMmFiYjIwNTFiMjQ4MWQwYmE3ZGVmZDYzNWNhN2E5MzMiCiAgICB9CiAgfQp9",
							"Tmqf1OSPk947yuNbxXEZ3OaZWPevClFCj8NU2WnlVTymsKq0XuqK3a1DsWrKWMHpOuhc4oFuQqKvwhDEniIshrulFOWR/eIUCHsSWBBAziGse61KiIt80KWmbUXThqlFu6sp/cPIPG30tcDsYHUl/g5dkLdPPPju8J9EHZoKhlms91OdvP3e9t6nTN/kIXhqy3bGgDE5pQWF+0E9KDZ7B3Oz0qrqqLYg6A/nCvzdctvsodmIdli5MoBCPUSIIt53xP8qZQL6gMtKfNSAA+S4un5k/K+gtDMd431oUefWwMHsFUubwPPl5XbOy4ys402++ymNPv5syn/daIG86Nax+XBX5Wxe56LeuG88Cu+7diBVUyMzT0+8xaImJGQvLQ4KtEQgZ1pLB8rWPPcDzLi7dfSmXgQWUWV1vJyk0876wvIVROCJRmvMVyuNDd4yagyW/e/ZB4o8V2iO6RCHQlCZbGgL1lPw91gFF7G4cEwKkfb6SqqTQuJ2QTT05p7MfjGtn2j/R3+h0+6fnx3rAwy34TPPN1WkZX/lPnb70KGWb2nGdvoE2Ab6szhsg7xHHw+SeSy9rDONyjhODFDdL/hbPLt/7VYtEy6x2lBKNVJ13gT6gNSeANpuLZmxMaXRQtEybQbbwUSmnS6BocFb1xaGRRdo0YmfKKiKFWIjxm0ga0Y="
						)
					)

					sparklyNPC.lookClose = true
					sparklyNPC.onClick {
						if (it.isSneaking) {
							it.sendMessage("Removing...")
							sparklyNPC.remove()
							return@onClick
						}

						it.sendMessage("hewwo!!! :3")

						sparklyNPC.setPlayerName(
							listOf("SparklyPower Survival", "Loritta Morenitta", "Pantufinha", "MrPowerGamerBR").random()
						)
					}

					/* val nmsWorld = (context.requirePlayer().world as CraftWorld).handle.minecraftWorld

					val fakePlayer = SparklyFakePlayer(nmsWorld)
					fakePlayer.setPos(player.location.x, player.location.y, player.location.z)
					nmsWorld.addFreshEntity(fakePlayer)

					val entity = fakePlayer.bukkitEntity as Husk

					entity.removeWhenFarAway = false
					entity.setAI(false)
					entity.isInvulnerable = true

					npcEntities[entity.uniqueId] = NPCData(

					)
					entity.persistentDataContainer.set(
						npcKey,
						true
					) */
					return
				}

				context.sendMessage("Creating...")

				val location = context.requirePlayer().location
				val selfPlayer = (context.requirePlayer() as CraftPlayer).handle
				val world = (context.requirePlayer().world as CraftWorld).handle.minecraftWorld
				val minecraftServer: MinecraftServer = (Bukkit.getServer() as CraftServer).server
				val serverLevel = world
				val gp = GameProfile(UUID.randomUUID(), "MrPowerGamerBR")
				val serverPlayer = ServerPlayer(
					minecraftServer,
					serverLevel,
					gp,
					ClientInformation.createDefault()
				)
				serverPlayer.setPos(location.getX(), location.getY(), location.getZ())

				val synchedEntityData = serverPlayer.entityData
				// Sets the skin data
				synchedEntityData[EntityDataAccessor(17, EntityDataSerializers.BYTE)] = 127.toByte()

				selfPlayer.connection.send(
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

				selfPlayer.connection.send(ClientboundAddEntityPacket(serverPlayer))
				// selfPlayer.connection.send(ClientboundSetEntityDataPacket(serverPlayer.id, synchedEntityData.nonDefaultValues))

				context.sendMessage("Created!")

				launchMainThread {
					delayTicks(100)

					selfPlayer.connection.send(ClientboundRemoveEntitiesPacket(serverPlayer.id))

					context.sendMessage("Bye!")
				}
			}
		}
	}

	class SparklyFakePlayer(world: Level) : net.minecraft.world.entity.monster.Husk(
		EntityType.HUSK,
		world
	) {
		override fun tick() {
			// Always reset the invulnerable time, allows players to kill the NPC
			this.invulnerableTime = 0

			// We only tick if we are dead or dying, this is from LivingEntity's baseTick code
			// This is needed because, if not, the entity is never removed from the world after being killed
			if (this.isDeadOrDying && level().shouldTickDeath(this)) {
				tickDeath()
			}
		}
	}

	fun spawnFakePlayer(location: Location, name: String): SparklyNPCData {
		val nmsWorld = (location.world as CraftWorld).handle

		val fakePlayer = SparklyFakePlayer(nmsWorld)
		val entity = fakePlayer.bukkitEntity as Husk

		fakePlayer.setPos(location.x, location.y, location.z)

		var fakePlayerName: String
		while (true) {
			val fakePlayerNameColors = (0 until 8).map { chatColors.random().toString() }
			fakePlayerName = fakePlayerNameColors.joinToString("")
			val isAlreadyBeingUsed = npcEntities.any { it.value.fakePlayerName == fakePlayerName }
			if (!isAlreadyBeingUsed)
				break
		}

		val npcData = SparklyNPCData(
			this,
			name,
			fakePlayerName,
			location,
			fakePlayer,
			entity
		)

		npcData.updateName()

		// We need to store the NPC data BEFORE we spawn them, to avoid the packet interceptor not intercepting the packets due to the NPC data being missing
		npcEntities[fakePlayer.uuid] = npcData

		println("NPC data has been set! ${fakePlayer.uuid}")

		entity.removeWhenFarAway = false
		entity.setAI(false)
		entity.isInvulnerable = true

		entity.persistentDataContainer.set(
			npcKey,
			true
		)

		nmsWorld.addFreshEntity(fakePlayer)

		return npcData
	}

	class SparklyNPCData(
		val m: SparklyDreamer,
		var name: String,
		val fakePlayerName: String,
		val initialLocation: Location,
		val nmsEntity: SparklyFakePlayer,
		val entity: Husk,
	) {
		internal var onLeftClickCallback: ((Player) -> (Unit))? = null
		internal var onRightClickCallback: ((Player) -> (Unit))? = null
		var lookClose = false

		/**
		 * Callback that will be invoked when clicking the NPC
		 */
		fun onClick(callback: ((Player) -> (Unit))?) {
			onLeftClickCallback = callback
			onRightClickCallback = callback
		}

		/**
		 * Teleports the NPC to the new [location]
		 */
		fun teleport(location: Location) {
			entity.teleport(location)
		}

		/**
		 * Deletes the NPC from the world
		 */
		fun remove() {
			nmsEntity.remove(Entity.RemovalReason.KILLED)
			m.npcEntities.remove(nmsEntity.uuid)
		}

		/**
		 * Sets the player name
		 */
		fun setPlayerName(name: String) {
			this.name = name

			updateName()
		}

		internal fun updateName() {
			val length = name.length
			val midpoint = length / 2
			val firstHalf = name.substring(0, midpoint)
			val secondHalf = name.substring(midpoint, length)

			val t = m.playerNames.getTeam(entity.uniqueId.toString()) ?: m.playerNames.registerNewTeam(entity.uniqueId.toString())
			t.prefix = firstHalf
			t.suffix = secondHalf

			Bukkit.broadcastMessage("First half is: $firstHalf")
			Bukkit.broadcastMessage("Second half is: $secondHalf")

			// "Identifiers for the entities in this team. For players, this is their username; for other entities, it is their UUID." - wiki.vg
			t.addEntry(fakePlayerName)
		}
	}
}