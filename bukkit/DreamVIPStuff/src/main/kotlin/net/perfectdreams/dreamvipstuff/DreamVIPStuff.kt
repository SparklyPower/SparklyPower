package net.perfectdreams.dreamvipstuff

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.citizensnpcs.api.CitizensAPI
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.node.HeldNode
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamvipstuff.commands.CabecasPersonalizadasCommand
import net.perfectdreams.dreamvipstuff.commands.RenomearCommand
import net.perfectdreams.dreamvipstuff.listeners.PlayerListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamVIPStuff : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§7Bottle§a§lXP§8]"
		private val vipNpcIds = listOf(
			447, 724, 725, 489, 456, 455, 741, 742, 743, 774, 775, 776, 778, 779, 780, 781, 782, 783
		)

		private val vipPlusNpcIds = listOf(
			744, 745, 746, 747, 748, 749, 750, 751, 752, 753, 754, 756, 757, 758, 759, 760, 761, 762
		)

		private val vipPlusPlusNpcIds = listOf(
			729, 732, 731, 735, 740, 736, 738, 739, 733, 726, 737, 763, 764, 765, 766, 767, 769, 770, 771, 772, 773, 768
		)
	}

	override fun softEnable() {
		super.softEnable()

		registerEvents(PlayerListener(this))
		registerCommand(CabecasPersonalizadasCommand)
		registerCommand(RenomearCommand)

		schedule {
			while (true) {
				switchContext(SynchronizationContext.ASYNC)
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

				switchContext(SynchronizationContext.SYNC)

				val alreadyCheckUsers = mutableListOf<UUID>()

				println(vipsPlusPlus.filter { !it.node.hasExpiry() }.mapNotNull { userDatas.firstOrNull { data -> data.id.value == it.holder } }.map { it.username })
				println(vipsPlus.filter { !it.node.hasExpiry() }.mapNotNull { userDatas.firstOrNull { data -> data.id.value == it.holder } }.map { it.username })
				println(vips.filter { !it.node.hasExpiry() }.mapNotNull { userDatas.firstOrNull { data -> data.id.value == it.holder } }.map { it.username })

				updateCitizensNPCs(alreadyCheckUsers, vipPlusPlusNpcIds, vipsPlusPlus.sortedBy { it.holder }, userDatas)
				updateCitizensNPCs(alreadyCheckUsers, vipPlusNpcIds, vipsPlus.sortedBy { it.holder }, userDatas)
				updateCitizensNPCs(alreadyCheckUsers, vipNpcIds, vips.sortedBy { it.holder }, userDatas)

				waitFor(20 * 60) // A cada um minuto
			}
		}
	}

	fun updateCitizensNPCs(alreadyCheckedUsers: MutableList<UUID>, npcIds: List<Int>, players: List<HeldNode<UUID>>, userDatas: List<User>) {
		val centerPosition = Location(Bukkit.getWorld("world"), 305.5, 66.0, 257.5)
		val npcs = npcIds.map {
			CitizensAPI.getNPCRegistry().getById(it)
		}.filterNotNull()
			.sortedBy { it.storedLocation.distanceSquared(centerPosition) }
			.toMutableList()

		for (heldNode in players) {
			val uniqueId = heldNode.holder
			val npc = npcs.firstOrNull() ?: continue
			val userData = userDatas.firstOrNull { it.id.value == uniqueId } ?: continue

			if (!npc.isSpawned) {
				logger.info { "Spawning $npc for $uniqueId because the NPC was despawned..." }
				npc.spawn(npc.storedLocation)
			}

			if (npc.name != userData.username) {
				logger.info { "Updating $npc name to be $uniqueId's name (${userData.username}) because the NPC has a different name ${npc.name}..." }
				npc.name = userData.username
			}

			alreadyCheckedUsers.add(uniqueId)
			npcs.remove(npc)
		}

		for (npc in npcs)
			npc.despawn()
	}
}