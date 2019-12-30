package net.perfectdreams.dreamcaixasecreta.listeners

import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import net.perfectdreams.dreamcaixasecreta.DreamCaixaSecreta
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.InstantFirework
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class BlockListener(val m: DreamCaixaSecreta) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onPlace(e: PlayerInteractEvent) {
		if (e.action != Action.RIGHT_CLICK_BLOCK)
			return

		if (e.item?.type != Material.CHEST)
			return

		val data = e.item.getStoredMetadata("caixaSecretaLevel") ?: return
		val level = data.toInt()

		e.isCancelled = true
		e.item.amount -= 1

		val items = mutableListOf<ItemStack>()
		var amount = 0

		for (item in m.prizes) {
			var chance = item.chance

			when (level) {
				4 -> chance *= 2
				3 -> chance *= 1.75
				2 -> chance *= 1.5
				1 -> chance *= 1.25
			}
			if (chance(chance)) {
				val itemStack = item.itemStack.clone()

				if (item.randomEnchant) {
					Enchantment.values()
							.filter { it.canEnchantItem(itemStack) }
							.forEach {
								if (chance(25 * chance)) {
									itemStack.addEnchantment(it, DreamUtils.random.nextInt(1, it.maxLevel + 1))
								}
							}
				}

				items.add(item.itemStack)
				amount += item.itemStack.amount
			}
		}

		val location = e.clickedBlock.location.add(0.5, 1.0, 0.5)
		if (items.isNotEmpty()) {
			items.forEach {
				e.player.world.dropItemNaturally(location, it)
			}

			val sp = RadioSongPlayer(m.itemReceived)
			sp.autoDestroy = true
			sp.addPlayer(e.player)
			sp.isPlaying = true

			InstantFirework.spawn(location, FireworkEffect.builder().withColor(Color.GREEN).flicker(true).trail(true).withFade(Color.YELLOW).with(FireworkEffect.Type.STAR).build())
			e.player.sendTitle("§aParabéns!", "§aVocê ganhou §9" + amount + " ite" + (if (items.size == 1) "m" else "ns") + "§a!", 10, 100, 10)
		} else {
			e.player.world.spawnParticle(Particle.VILLAGER_ANGRY, location, 5, 0.5, 0.5, 0.5);
			e.player.sendTitle("§cQue pena...", "§cVocê não ganhou nada...", 10, 100, 10);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onBlock(e: BlockBreakEvent) {
		if (e.block.type != Material.STONE)
			return

		if (e.player.inventory.itemInMainHand?.containsEnchantment(Enchantment.SILK_TOUCH) == true)
			return

		val chance = 0.8

		if (chance(chance)) {
			val random = DreamUtils.random.nextInt(0, 100)

			val level = when (random) {
				in 99..99 -> 4
				in 90..98 -> 3
				in 75..89 -> 2
				in 60..74 -> 1
				else -> 0
			}

			val item = m.generateCaixaSecreta(level)

			e.player.world.dropItemNaturally(e.block.location, item)
		}
	}
}