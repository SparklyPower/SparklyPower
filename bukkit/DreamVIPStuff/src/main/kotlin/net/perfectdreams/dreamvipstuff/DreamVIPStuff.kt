package net.perfectdreams.dreamvipstuff

import kotlinx.coroutines.delay
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.HeldNode
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import net.perfectdreams.dreamcore.utils.npc.SparklyNPC
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamvipstuff.commands.BackCommand
import net.perfectdreams.dreamvipstuff.commands.CabecasPersonalizadasCommand
import net.perfectdreams.dreamvipstuff.commands.RenomearCommand
import net.perfectdreams.dreamvipstuff.listeners.PlayerListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.minutes

class DreamVIPStuff : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§7Bottle§a§lXP§8]"
	}

	private val vipPlusPlusNpcsData = mutableListOf<SpawnedVIPNPC>()
	private val vipPlusNpcsData = mutableListOf<SpawnedVIPNPC>()
	private val vipNpcsData = mutableListOf<SpawnedVIPNPC>()

	val storedLocations = mutableMapOf<UUID, Location>()

	override fun softEnable() {
		super.softEnable()

		// We declare the NPC locations here because we KNOW that when the plugin is enabled, the "world" is already loaded
		// Whereas if we declared it on root level, the world isn't loaded!
		val vipNpcLocations = listOf(
			Location(Bukkit.getWorld("world"), 269.5134, 72.0, 296.4704, 55.055f, -59.6215f), // old Citizens NPC ID: 1057
			Location(Bukkit.getWorld("world"), 293.5119, 72.0, 296.5353, -138.0864f, 27.589f), // old Citizens NPC ID: 1056
			Location(Bukkit.getWorld("world"), 290.5099, 74.0, 295.5159, 137.4481f, 28.1641f), // old Citizens NPC ID: 1055
			// NPC data not found: 1054
			Location(Bukkit.getWorld("world"), 275.474, 73.0, 295.4826, 176.5262f, -6.8467f), // old Citizens NPC ID: 1053
			Location(Bukkit.getWorld("world"), 272.563, 74.0, 295.4755, -130.9375f, -17.0367f), // old Citizens NPC ID: 1052
			Location(Bukkit.getWorld("world"), 279.5053, 73.0, 294.4692, 110.3836f, 19.2747f), // old Citizens NPC ID: 1051
			Location(Bukkit.getWorld("world"), 283.4775, 73.0, 294.4784, -177.4222f, -11.9574f), // old Citizens NPC ID: 1050
			Location(Bukkit.getWorld("world"), 293.5082, 72.0, 293.5206, -67.3604f, 42.1579f), // old Citizens NPC ID: 1049
			Location(Bukkit.getWorld("world"), 288.5258, 72.0, 293.5084, 0.7419f, 90.0f), // old Citizens NPC ID: 1048
			Location(Bukkit.getWorld("world"), 285.4845, 72.0, 293.4943, -109.858f, 22.3494f), // old Citizens NPC ID: 1047
			Location(Bukkit.getWorld("world"), 281.5113, 74.0, 293.4842, 142.1761f, 42.4367f), // old Citizens NPC ID: 1046
			Location(Bukkit.getWorld("world"), 277.5169, 72.0, 293.5265, 139.1844f, 43.0289f), // old Citizens NPC ID: 1045
			Location(Bukkit.getWorld("world"), 274.5, 72.0, 293.5094, 143.8802f, 39.2483f), // old Citizens NPC ID: 1044
			Location(Bukkit.getWorld("world"), 269.4856, 72.0, 293.4676, 114.8604f, 32.1503f), // old Citizens NPC ID: 1043
			Location(Bukkit.getWorld("world"), 272.5167, 73.0, 292.501, 95.7587f, 45.6381f), // old Citizens NPC ID: 1042
			Location(Bukkit.getWorld("world"), 279.5059, 72.0, 292.5126, 104.6462f, 40.1913f), // old Citizens NPC ID: 1041
			Location(Bukkit.getWorld("world"), 283.4978, 72.0, 292.5154, -118.1454f, -0.4427f), // old Citizens NPC ID: 1040
			Location(Bukkit.getWorld("world"), 290.4737, 73.0, 292.5192, 172.3918f, 47.6666f), // old Citizens NPC ID: 1039
			Location(Bukkit.getWorld("world"), 292.5107, 71.0, 290.505, -72.5035f, 9.8071f), // old Citizens NPC ID: 1038
			Location(Bukkit.getWorld("world"), 287.525, 72.0, 290.526, -40.7393f, 34.5506f), // old Citizens NPC ID: 447
			Location(Bukkit.getWorld("world"), 281.497, 71.0, 290.481, -93.3445f, -15.5776f), // old Citizens NPC ID: 724
			Location(Bukkit.getWorld("world"), 275.527, 72.0, 290.508, -118.6878f, -3.5121f), // old Citizens NPC ID: 725
			Location(Bukkit.getWorld("world"), 270.489, 71.0, 290.49, -114.1724f, 0.0f), // old Citizens NPC ID: 489
			Location(Bukkit.getWorld("world"), 273.51, 71.0, 289.486, 122.3327f, 0.2018f), // old Citizens NPC ID: 456
			Location(Bukkit.getWorld("world"), 277.513, 71.0, 289.405, -137.4523f, 11.071f), // old Citizens NPC ID: 455
			Location(Bukkit.getWorld("world"), 279.515, 71.0, 289.409, -89.4424f, 10.3202f), // old Citizens NPC ID: 741
			Location(Bukkit.getWorld("world"), 283.5, 71.0, 289.423, -60.1055f, -17.6858f), // old Citizens NPC ID: 742
			Location(Bukkit.getWorld("world"), 285.478, 71.0, 289.49, -36.0608f, -2.9255f), // old Citizens NPC ID: 743
			Location(Bukkit.getWorld("world"), 289.484, 71.0, 289.483, -85.7311f, 11.2524f), // old Citizens NPC ID: 774
			Location(Bukkit.getWorld("world"), 291.549, 71.0, 287.511, -36.5427f, -4.9116f), // old Citizens NPC ID: 775
			Location(Bukkit.getWorld("world"), 287.463, 71.0, 287.485, -68.4711f, -0.0221f), // old Citizens NPC ID: 776
			Location(Bukkit.getWorld("world"), 284.502, 70.0, 287.518, 160.8118f, 0.0f), // old Citizens NPC ID: 778
			Location(Bukkit.getWorld("world"), 281.484, 70.0, 287.496, -131.5289f, 0.0f), // old Citizens NPC ID: 779
			Location(Bukkit.getWorld("world"), 278.493, 70.0, 287.488, -126.7398f, 0.0f), // old Citizens NPC ID: 780
			Location(Bukkit.getWorld("world"), 275.542, 71.0, 287.459, 103.3878f, 0.2505f), // old Citizens NPC ID: 781
			Location(Bukkit.getWorld("world"), 271.513, 71.0, 287.453, 109.8885f, 0.3827f), // old Citizens NPC ID: 783
		)

		val vipPlusNpcLocations = listOf(
			Location(Bukkit.getWorld("world"), 293.5064, 72.0, 218.4789, 90.1395f, -45.2597f), // old Citizens NPC ID: 1077
			Location(Bukkit.getWorld("world"), 269.4389, 72.0, 218.4039, -45.5568f, -15.7756f), // old Citizens NPC ID: 1076
			Location(Bukkit.getWorld("world"), 272.5116, 74.0, 219.4738, -3.1381f, 63.6897f), // old Citizens NPC ID: 1075
			Location(Bukkit.getWorld("world"), 275.5254, 73.0, 219.4625, 115.3204f, 10.171f), // old Citizens NPC ID: 1074
			Location(Bukkit.getWorld("world"), 287.5173, 73.0, 219.5327, -146.218f, 51.8126f), // old Citizens NPC ID: 1073
			Location(Bukkit.getWorld("world"), 290.56, 74.0, 219.5336, 84.8905f, -3.354f), // old Citizens NPC ID: 1072
			Location(Bukkit.getWorld("world"), 283.4346, 73.0, 220.463, 74.4911f, -21.2529f), // old Citizens NPC ID: 1071
			Location(Bukkit.getWorld("world"), 279.5313, 73.0, 220.4619, 75.2553f, 16.5882f), // old Citizens NPC ID: 1070
			Location(Bukkit.getWorld("world"), 269.4112, 72.0, 221.507, 28.7297f, 32.1655f), // old Citizens NPC ID: 1069
			Location(Bukkit.getWorld("world"), 274.4595, 72.0, 221.5079, 116.6182f, 43.4381f), // old Citizens NPC ID: 1068
			Location(Bukkit.getWorld("world"), 277.5154, 72.0, 221.5085, 133.7742f, -20.3618f), // old Citizens NPC ID: 1067
			Location(Bukkit.getWorld("world"), 281.4913, 74.0, 221.5099, -32.2499f, 42.1922f), // old Citizens NPC ID: 1066
			Location(Bukkit.getWorld("world"), 285.4645, 72.0, 221.5103, -33.9023f, 21.7022f), // old Citizens NPC ID: 1065
			Location(Bukkit.getWorld("world"), 288.6268, 72.0, 221.5118, -179.4222f, 16.0501f), // old Citizens NPC ID: 1064
			Location(Bukkit.getWorld("world"), 293.4778, 72.0, 221.5125, 126.6925f, -22.2645f), // old Citizens NPC ID: 1063
			Location(Bukkit.getWorld("world"), 290.4278, 73.0, 222.5355, 148.8375f, 59.4515f), // old Citizens NPC ID: 1062
			Location(Bukkit.getWorld("world"), 283.5023, 72.0, 222.5338, -66.524f, 15.7448f), // old Citizens NPC ID: 1061
			Location(Bukkit.getWorld("world"), 279.4514, 72.0, 222.5329, -71.9273f, -5.0258f), // old Citizens NPC ID: 1060
			Location(Bukkit.getWorld("world"), 272.4554, 73.0, 222.5604, 55.6335f, 16.4288f), // old Citizens NPC ID: 1059
			Location(Bukkit.getWorld("world"), 270.4839, 71.0, 224.4668, -40.5821f, 0.0f), // old Citizens NPC ID: 1058
			Location(Bukkit.getWorld("world"), 275.553, 72.0, 224.549, -35.3821f, -0.0256f), // old Citizens NPC ID: 744
			Location(Bukkit.getWorld("world"), 281.479, 71.0, 224.519, -8.041f, -0.0267f), // old Citizens NPC ID: 745
			Location(Bukkit.getWorld("world"), 287.493, 72.0, 224.496, 49.4246f, -3.38f), // old Citizens NPC ID: 746
			Location(Bukkit.getWorld("world"), 292.53, 71.0, 224.491, 168.1401f, -38.0808f), // old Citizens NPC ID: 747
			Location(Bukkit.getWorld("world"), 289.518, 71.0, 225.465, 117.1087f, -23.2211f), // old Citizens NPC ID: 748
			Location(Bukkit.getWorld("world"), 285.517, 71.0, 225.472, -102.4628f, -19.7209f), // old Citizens NPC ID: 749
			Location(Bukkit.getWorld("world"), 283.475, 71.0, 225.473, -24.616f, 22.5256f), // old Citizens NPC ID: 750
			Location(Bukkit.getWorld("world"), 279.456, 71.0, 225.475, -48.3722f, 5.0675f), // old Citizens NPC ID: 751
			Location(Bukkit.getWorld("world"), 277.517, 71.0, 225.476, -52.3143f, -2.506f), // old Citizens NPC ID: 752
			Location(Bukkit.getWorld("world"), 273.484, 71.0, 225.503, -62.0997f, 0.0f), // old Citizens NPC ID: 753
			Location(Bukkit.getWorld("world"), 271.478, 71.0, 227.529, -104.6525f, -3.2125f), // old Citizens NPC ID: 754
			Location(Bukkit.getWorld("world"), 275.51, 71.0, 227.498, -101.0696f, -27.1394f), // old Citizens NPC ID: 756
			Location(Bukkit.getWorld("world"), 278.494, 70.0, 227.453, -93.3913f, -8.8487f), // old Citizens NPC ID: 758
			Location(Bukkit.getWorld("world"), 281.451, 70.0, 227.484, -26.9197f, 0.0f), // old Citizens NPC ID: 759
			Location(Bukkit.getWorld("world"), 284.463, 70.0, 227.509, 103.7421f, -8.3728f), // old Citizens NPC ID: 760
			Location(Bukkit.getWorld("world"), 287.48, 71.0, 227.53, 0.6795f, -10.5287f), // old Citizens NPC ID: 761
			Location(Bukkit.getWorld("world"), 291.486, 71.0, 227.493, 54.0691f, 9.7406f), // old Citizens NPC ID: 762
		)

		val vipPlusPlusNpcLocations = listOf(
			Location(Bukkit.getWorld("world"), 257.5129, 74.0, 266.4901, -160.3731f, -8.231f), // old Citizens NPC ID: 1037
			Location(Bukkit.getWorld("world"), 257.5129, 73.0, 263.505, 124.144f, 16.1604f), // old Citizens NPC ID: 1036
			Location(Bukkit.getWorld("world"), 257.5068, 73.0, 251.5176, -70.2383f, -43.4858f), // old Citizens NPC ID: 1035
			Location(Bukkit.getWorld("world"), 257.505, 74.0, 248.4993, 163.2217f, 76.2432f), // old Citizens NPC ID: 1034
			Location(Bukkit.getWorld("world"), 258.581, 72.0, 246.5073, -101.5868f, -43.443f), // old Citizens NPC ID: 1033
			Location(Bukkit.getWorld("world"), 258.5834, 73.0, 255.4873, -127.8102f, -2.6769f), // old Citizens NPC ID: 1032
			Location(Bukkit.getWorld("world"), 258.5866, 73.0, 259.5501, -125.3514f, -2.1083f), // old Citizens NPC ID: 1031
			Location(Bukkit.getWorld("world"), 258.5898, 72.0, 268.5106, 172.2773f, 42.8433f), // old Citizens NPC ID: 1030
			Location(Bukkit.getWorld("world"), 259.4445, 72.0, 264.5504, -121.4677f, -32.3657f), // old Citizens NPC ID: 1029
			Location(Bukkit.getWorld("world"), 259.522, 72.0, 261.522, -154.1389f, -21.5254f), // old Citizens NPC ID: 729
			Location(Bukkit.getWorld("world"), 259.531, 74.0, 257.523, -162.4229f, 14.9916f), // old Citizens NPC ID: 732
			Location(Bukkit.getWorld("world"), 259.542, 72.0, 253.503, -96.6542f, -21.9962f), // old Citizens NPC ID: 731
			Location(Bukkit.getWorld("world"), 259.549, 72.0, 250.523, 3.9027f, -60.1815f), // old Citizens NPC ID: 735
			Location(Bukkit.getWorld("world"), 260.575, 73.0, 248.473, -163.9766f, 51.1972f), // old Citizens NPC ID: 740
			Location(Bukkit.getWorld("world"), 260.526, 72.0, 255.508, -142.6022f, -20.9869f), // old Citizens NPC ID: 736
			Location(Bukkit.getWorld("world"), 260.491, 72.0, 259.522, -152.3494f, -19.7942f), // old Citizens NPC ID: 738
			Location(Bukkit.getWorld("world"), 260.539, 73.0, 266.54, -149.7022f, -13.1784f), // old Citizens NPC ID: 739
			Location(Bukkit.getWorld("world"), 262.5, 72.0, 263.591, -178.245f, -29.8414f), // old Citizens NPC ID: 733
			Location(Bukkit.getWorld("world"), 262.513, 71.0, 257.505, 150.2161f, -45.1702f), // old Citizens NPC ID: 726
			Location(Bukkit.getWorld("world"), 262.528, 72.0, 251.553, -72.4827f, -30.9927f), // old Citizens NPC ID: 737
			Location(Bukkit.getWorld("world"), 263.482, 71.0, 249.419, -12.4365f, -45.806f), // old Citizens NPC ID: 763
			Location(Bukkit.getWorld("world"), 263.47, 71.0, 253.522, -76.9456f, -32.6131f), // old Citizens NPC ID: 764
			Location(Bukkit.getWorld("world"), 263.467, 71.0, 255.485, 84.6465f, -46.5558f), // old Citizens NPC ID: 765
			Location(Bukkit.getWorld("world"), 263.499, 71.0, 259.509, 75.8065f, -61.9451f), // old Citizens NPC ID: 766
			Location(Bukkit.getWorld("world"), 263.539, 71.0, 261.535, 145.261f, -62.7618f), // old Citizens NPC ID: 767
			Location(Bukkit.getWorld("world"), 263.594, 71.0, 265.537, -63.5281f, -12.6505f), // old Citizens NPC ID: 769
			Location(Bukkit.getWorld("world"), 265.507, 71.0, 263.499, -40.5728f, 18.2936f), // old Citizens NPC ID: 770
			Location(Bukkit.getWorld("world"), 265.486, 70.0, 260.478, -44.9458f, -12.2497f), // old Citizens NPC ID: 771
			Location(Bukkit.getWorld("world"), 265.457, 70.0, 257.45, -45.9816f, -20.4548f), // old Citizens NPC ID: 772
			Location(Bukkit.getWorld("world"), 265.447, 70.0, 254.436, 153.3447f, 0.0f), // old Citizens NPC ID: 773
			// NPC data not found: 768
		)

		val vipsCenterLocation = Location(Bukkit.getWorld("world"), 281.0, 70.0, 281.0).toCenterLocation()
		val vipsPlusCenterLocation = Location(Bukkit.getWorld("world"), 281.0, 70.0, 233.0).toCenterLocation()
		val vipsPlusPlusCenterLocation = Location(Bukkit.getWorld("world"), 271.0, 70.0, 257.0).toCenterLocation()

		registerEvents(PlayerListener(this))
		registerCommand(BackCommand(this))
		registerCommand(CabecasPersonalizadasCommand)
		registerCommand(RenomearCommand)

		launchAsyncThread {
			while (true) {
				val api = LuckPermsProvider.get()

				val vipsPlusPlusJob = api.userManager.getWithPermission("group.vip++")
				val vipsPlusJob = api.userManager.getWithPermission("group.vip+")
				val vipsJob = api.userManager.getWithPermission("group.vip")

				val vipsPlusPlus = vipsPlusPlusJob.get()
				val vipsPlus = vipsPlusJob.get()
				val vips = vipsJob.get()

				val userDatas = transaction(Databases.databaseNetwork) {
					User.wrapRows(
						Users.select {
							Users.id inList (vipsPlusPlus.map { it.holder } + vipsPlus.map { it.holder } + vips.map { it.holder }).distinct()
						}
					).toList()
				}

				onMainThread {
					updateSparklyNPCs(VIPType.VIP_PLUS_PLUS, vipsPlusPlusCenterLocation, vipPlusPlusNpcsData, vipPlusPlusNpcLocations, vipsPlusPlus.sortedBy { it.holder }, userDatas)
					updateSparklyNPCs(VIPType.VIP_PLUS, vipsPlusCenterLocation, vipPlusNpcsData, vipPlusNpcLocations, vipsPlus.sortedBy { it.holder }, userDatas)
					updateSparklyNPCs(VIPType.VIP, vipsCenterLocation, vipNpcsData, vipNpcLocations, vips.sortedBy { it.holder }, userDatas)
				}

				delay(1.minutes) // Every one minute
			}
		}
	}

	private suspend fun updateSparklyNPCs(
		type: VIPType,
		npcsLookAtLocation: Location,
		npcsData: MutableList<SpawnedVIPNPC>,
		npcLocations: List<Location>,
		players: List<HeldNode<UUID>>,
		userDatas: List<User>
	) {
		// Check if we need to spawn or despawn any NPCs
		val centerPosition = Location(Bukkit.getWorld("world"), 305.5, 66.0, 257.5)

		val locationsSortedByDistance = npcLocations
			.sortedBy { it.distanceSquared(npcsLookAtLocation) }
			.toMutableList()

		// This is hard because we need to "reshuffle" the players
		val playerIds = players.map { it.holder }
			.sortedBy { it } // We sort it to have a consistent list
			.take(locationsSortedByDistance.size) // aaaand we only take how many locations we have, to avoid check failures if we have more players than NPC slots

		// Now, this is hard, because if ANYONE on the list changes, we must delete all the entities and respawn them
		// For this, we are going to check if EVERYONE is already spawned
		val alreadySpawnedPlayerIds = npcsData.map { it.player }

		if (alreadySpawnedPlayerIds.containsAll(playerIds) && playerIds.containsAll(alreadySpawnedPlayerIds)) {
			logger.info { "Skipping update for $type because all the NPCs are already spawned!" }
			return // All of them are already spawned! Bail out because we don't need to update
		}

		logger.info { "Updating $type NPCs..." }

		// Okay, so the lists are *actually* different, so, to fix this...
		// Delete ALL spawned NPCs
		npcsData.forEach {
			it.sparklyNPC.remove()
		}

		// Clear the VIP NPCs data
		npcsData.clear()

		// Respawn them!
		for (playerId in playerIds) {
			// This should NEVER be null since we only take how many slots we have
			val slot = locationsSortedByDistance.removeAt(0)
			val userData = userDatas.firstOrNull { it.id.value == playerId }

			// Must be in an async thread because here we are in a sync thread
			val skinTextures = onAsyncThread {
				DreamCore.INSTANCE.skinUtils.retrieveSkinTexturesBySparklyPowerUniqueId(playerId)
					?.let {
						SkinTexture(it.value, it.signature!!)
					}
			}

			val directionX = npcsLookAtLocation.x - slot.x
			val directionY = npcsLookAtLocation.y - slot.y
			val directionZ = npcsLookAtLocation.z - slot.z

			// Calculate yaw (horizontal angle)
			val yaw = Math.toDegrees(atan2(-directionX, directionZ))

			// Calculate pitch (vertical angle)
			val horizontalDistance = sqrt(directionX * directionX + directionZ * directionZ)
			val pitch = Math.toDegrees(atan2(-directionY, horizontalDistance))

			val sparklyNPC = DreamCore.INSTANCE.sparklyNPCManager.spawnFakePlayer(
				this,
				slot.clone()
					.apply {
						this.yaw = yaw.toFloat()
						this.pitch = pitch.toFloat()
					},
				userData?.username ?: "???",
				skinTextures = skinTextures
			)

			// And add them to the vipNpcsData list
			npcsData.add(
				SpawnedVIPNPC(
					sparklyNPC,
					playerId,
					slot
				)
			)

			logger.info { "Created NPC for ${userData?.username} (${playerId}) at $slot!" }
		}
	}

	data class SpawnedVIPNPC(
		val sparklyNPC: SparklyNPC,
		val player: UUID,
		val spawnedLocation: Location
	)

	enum class VIPType {
		VIP,
		VIP_PLUS,
		VIP_PLUS_PLUS
	}
}