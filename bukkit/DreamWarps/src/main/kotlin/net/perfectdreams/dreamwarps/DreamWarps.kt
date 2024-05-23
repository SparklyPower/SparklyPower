package net.perfectdreams.dreamwarps

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamwarps.commands.DreamWarpCommand
import net.perfectdreams.dreamwarps.commands.WarpCommand
import net.perfectdreams.dreamwarps.utils.Warp
import net.perfectdreams.dreamwarps.utils.WarpInventoryHolder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.io.File

class DreamWarps : KotlinPlugin(), Listener {
	companion object {
		val PREFIX = "§8[§5§lGPS§8]§e"
	}

	var warps = mutableListOf<Warp>()

	val warpsMenu = createMenu(27, "§3§lPara aonde você deseja ir?") {
		slot(3, 0) {
			item = ItemStack(Material.OAK_PLANKS)
				.rename("§a§lLocal para Construir")
				.lore(
					"§7Sua aventura no SparklyPower começa aqui.",
					"§7O lugar aonde você pode construir suas incríveis coisas!",
					"§7",
					"§7Você pode proteger as suas coisas usando a pá de ouro",
					"§7do §6/kit noob§7!",
					"§7",
					"§7Clique aqui para ir ao survival! (§6/warp survival1§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp survival")
			}
		}
		slot(3, 1) {
			item = ItemStack(Material.ACACIA_PLANKS)
				.rename("§a§lLocal para Construir 2")
				.lore(
					"§7Sua aventura no SparklyPower começa aqui.",
					"§7O lugar aonde você pode construir suas incríveis coisas!",
					"§7",
					"§7Você pode proteger as suas coisas usando a pá de ouro",
					"§7do §6/kit noob§7!",
					"§7",
					"§7Clique aqui para ir ao survival! (§6/warp survival2§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp survival2")
			}
		}
		slot(5, 0) {
			item = ItemStack(Material.DIAMOND_PICKAXE)
				.rename("§a§lMundo de Recursos")
				.lore(
					"§7Sua aventura no SparklyPower também começa aqui.",
					"§7O lugar aonde você pode minerar e pegar recursos!",
					"§7",
					"§7O mundo reseta a cada semana, caso você queria construir,",
					"§7vá na §6/warp survival§7!",
					"§7",
					"§7Clique aqui para ir ao mundo de recursos! (§6/warp recursos§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp recursos")
			}
		}
		slot(8, 0) {
			item = ItemStack(Material.ANVIL)
				.rename("§7§lReparar & Encantar")
				.lore(
					"§7Seus itens estão precisando de uma... ajudinha?",
					"§7Cansado de quebrar a sua incrível picareta encantada?",
					"§7",
					"§7Então clique aqui para ir na sala de reparar & encantar! (§6/warp reparar§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp reparar")
			}
		}
		slot(4, 0) {
			item = ItemStack(Material.NETHER_STAR)
				.rename("§6§lLoja Oficial")
				.lore(
					"§7Precisando comprar e vender itens?",
					"§7",
					"§7Então clique aqui para ir na loja! (§6/warp loja§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp loja")
			}
		}
		slot(6, 2) {
			item = ItemStack(Material.MINECART)
				.rename("§b§lConcessionária")
				.lore(
					"§7Carros para você andar pelo servidor!",
					"§7",
					"§7Então clique aqui para ir na concessionária! (§6/warp concessionaria§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp concessionaria")
			}
		}
		slot(4, 1) {
			item = ItemStack(Material.EMERALD)
				.rename("§a§lRecanto dos VIP")
				.lore(
					"§7Apenas para VIPs, rs",
					"§7",
					"§7Então clique aqui para ir na loja VIP! (§6/warp vip§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp vip")
			}
		}
		slot(7, 1) {
			item = ItemStack(Material.SPAWNER)
				.rename("§5§lSpawner")
				.lore(
					"§7Quer matar alguns mobs?",
					"§7",
					"§7Então clique aqui para ir para o spawner! (§6/warp spawner§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp spawner")
			}
		}
		slot(8, 1) {
			item = ItemStack(Material.PLAYER_HEAD)
				.rename("§6§lDecorações")
				.lore(
					"§7Precisando decorar a sua casa?",
					"§7",
					"§7Então clique aqui para ir na loja de decorativos! (§6/warp decorações§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp decorações")
			}
		}
		slot(0, 0) {
			item = ItemStack(Material.BOOK)
				.rename("§5§lEquipe e Regras")
				.lore(
					"§7Curioso para ver quem mantém o servidor?",
					"§7Quer ver as regras do servidor?",
					"§7",
					"§7Então clique aqui para ver a equipe e as regras! (§6/warp staff§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp staff")
			}
		}
		slot(7, 2) {
			item = ItemStack(Material.NETHERRACK)
				.rename("§4§lNether")
				.lore(
					"§7Nether, o inferno, aonde o demônio vive.",
					"§7Lugar perfeito para pegar aquele bronzeado! :3",
					"§7",
					"§4§lCuidado!§7 PvP está ativado e você perde itens caso morrer no Nether!",
					"§7",
					"§7Mapa é resetado a cada uma semana!",
					"§7",
					"§7Clique aqui para ir ao Nether (§6/warp nether§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp nether")
			}
		}
		slot(8, 2) {
			item = ItemStack(Material.END_STONE)
				.rename("§5§lThe End")
				.lore(
					"§7The End.",
					"§7",
					"§4§lCuidado!§7 PvP está ativado e você perde itens caso morrer no The End!",
					"§7",
					"§7Mapa é resetado a cada uma semana!",
					"§7",
					"§7Clique aqui para ir ao The End (§6/warp end§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp end")
			}
		}

		slot(0, 2) {
			item = ItemStack(Material.DIAMOND_SWORD)
				.rename("§c§lMini Arena PvP")
				.lore(
					"§7Preparado para testar as suas habilidades PvP",
					"§7contra outros players do servidor?",
					"§7Quer resolver a briga com um PvP mano a mano?",
					"§7",
					"§7Então visite a nossa Mini Arena PvP, o lugar aonde",
					"§7até os mais fortes sucumbem da fama.",
					"§7",
					"§4§lCuidado!§7 PvP está ativado e você perde itens caso morrer na arena!",
					"§7",
					"§7Clique aqui para ir a Mini Arena PvP (§6/warp miniarena§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp miniarena")
			}
		}
		slot(1, 2) {
			item = ItemStack(Material.IRON_SWORD)
				.rename("§c§lArena FPS")
				.lore(
					"§7Você quer ir PvP, mas está com medo de morrer porque",
					"§7o seu PC é movido a manivela e pode dar aquele lag",
					"§7maroto no FPS a qualquer momento?",
					"§7",
					"§7Então visite a nossa Arena FPS, uma arena 99% invisível para",
					"§7você lutar contra outros players sem preocupações de lag!",
					"§7",
					"§4§lCuidado!§7 PvP está ativado e você perde itens caso morrer na arena!",
					"§7",
					"§7Clique aqui para ir a Arena FPS (§6/warp arenafps§7)"
				)

			onClick { clicker ->
				(clicker as Player).performCommand("warp arenafps")
			}
		}
	}

	fun loadWarpsMenu() {
		val menuFile = File(dataFolder, "warp_menu.kts")

		val source = menuFile.readText()

		println(source)
	}

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		loadConfig()
		loadWarpsMenu()

		registerCommand(WarpCommand(this))
		registerCommand(DreamWarpCommand(this))
		registerEvents(this)

		updateChunkTickets()
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun loadConfig() {
		warps.clear()
		reloadConfig()

		val warpsFolder = File(dataFolder, "warps")
		warpsFolder.mkdirs()

		warpsFolder.listFiles().forEach {
			if (it.extension == "json") {
				warps.add(DreamUtils.gson.fromJson(it.readText()))
			}
		}
	}

	fun saveWarps() {
		val warpsFolder = File(dataFolder, "warps")
		warpsFolder.deleteRecursively()
		warpsFolder.mkdirs()

		for (warp in warps) {
			File(warpsFolder, "${warp.name}.json").writeText(DreamUtils.gson.toJson(warp))
		}
	}

	@EventHandler
	fun onClick(e: InventoryClickEvent) {
		val holder = e.inventory.holder ?: return

		if (holder !is WarpInventoryHolder)
			return

		e.isCancelled = true
		val player = e.whoClicked as Player
		player.closeInventory()

		val currentItem = e.currentItem ?: return
		if (currentItem.type == Material.AIR)
			return

		val data = e.currentItem?.getStoredMetadata("warpName") ?: return

		player.performCommand("dwarps $data")
	}

	fun updateChunkTickets() {
		val worlds = Bukkit.getWorlds()
		worlds.forEach {
			it.removePluginChunkTickets(this)
		}

		warps.forEach {
			it.location.chunk.addPluginChunkTicket(this)
		}
	}
}