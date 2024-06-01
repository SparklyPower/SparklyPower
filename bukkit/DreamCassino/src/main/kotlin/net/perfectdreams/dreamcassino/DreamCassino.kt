package net.perfectdreams.dreamcassino

import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamcore.utils.*
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Powerable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import java.io.File

class DreamCassino : KotlinPlugin(), Listener {
	companion object {
		const val PREFIX = "§8[§a§lCassino§8]§e"
	}

	lateinit var config: CassinoConfig

	override fun softEnable() {
		super.softEnable()

		this.dataFolder.mkdirs()
		val configFile = File(dataFolder, "config.json")

		if (!configFile.exists()) {
			configFile.createNewFile()
			configFile.writeText(DreamUtils.gson.toJson(CassinoConfig()))
		}

		config = DreamUtils.gson.fromJson(configFile.readText(), CassinoConfig::class.java)

		registerEvents(this)
		registerCommand(CassinoCommand(this))
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onInteract(e: PlayerInteractEvent) {
		val clickedBlock = e.clickedBlock ?: return
		val type = clickedBlock.type

		if (type != Material.LEVER)
			return

		// Ok, é uma alavanca, mas e agora? :megathink:
		val block = e.clickedBlock ?: return

		val directional = block.state.blockData as Directional
		val powerable = block.state.blockData as Powerable

		val face = directional.facing.oppositeFace

		val relativeBlock = block.getRelative(face)

		fun getAttachedBlocks(block: Block): Set<Block> {
			return BlockFace.values().map { block.getRelative(it) }.toSet()
		}

		val signBlock = getAttachedBlocks(relativeBlock).firstOrNull { it.type.name.contains("SIGN") } ?: return
		val sign = signBlock.state as Sign

		// Não é uma placa de cassino :sad_cat:
		if (sign.getLine(0) != "§1[Cassino]")
			return

		if (powerable.isPowered) {
			e.isCancelled = true
			return
		}

		if (50 > e.player.balance) {
			e.isCancelled = true
			e.player.sendMessage("${PREFIX} §cVocê precisa ter §2+${50 - e.player.balance} Sonecas§c para poder apostar!")
			return
		}

		e.player.sendMessage("${PREFIX} §aVocê apostou §250 Sonecas§a!")
		e.player.withdraw(250.00, TransactionContext(type = TransactionType.BETTING, extra = "no `cassino`"))

		val randomNumber1 = DreamUtils.random.nextInt(0, 20)
		val randomNumber2 = DreamUtils.random.nextInt(0, 20)
		val randomNumber3 = DreamUtils.random.nextInt(0, 20)

		val player = e.player

		fun getValueForNumber(number: Int): String {
			if (number in 10 until 14) {
				return "Y"
			}
			if (number in 14 until 17) {
				return "Z"
			}
			if (number in 17 until 19) {
				return "A"
			}
			if (number == 19) {
				return "J"
			}
			return "X"
		}

		val value1 = getValueForNumber(randomNumber1)
		val value2 = getValueForNumber(randomNumber2)
		val value3 = getValueForNumber(randomNumber3)

		val prizes = mutableMapOf(
			"X" to 125,
			"Y" to 250,
			"Z" to 500,
			"A" to 750
		)

		scheduler().schedule(this) {
			val position = e.player.location.add(0.0, 1.0, 0.0)

			sign.setLine(0, "§1[Cassino]")
			sign.setLine(1, "§k$value1§0|§k$value2§0|§k$value3")
			sign.update()
			waitFor(20)
			sign.setLine(1, "$value1§0|§k$value2§0|§k$value3")
			sign.setLine(2, "§d§lVENÇA!")
			sign.setLine(3, "§d(｡◕‿‿◕｡)")
			sign.update()
			e.player.world.playSound(clickedBlock.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
			waitFor(20)
			sign.setLine(1, "$value1§0|$value2§0|§k$value3")
			sign.setLine(2, "§5§lVENÇA!")
			sign.setLine(3, "§5(｡◕‿‿◕｡)")
			sign.update()
			e.player.world.playSound(clickedBlock.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
			waitFor(20)
			sign.setLine(1, "$value1§0|$value2§0|$value3")
			sign.setLine(2, "§d§lVENÇA!")
			sign.setLine(3, "§d(｡◕‿‿◕｡)")
			sign.update()
			e.player.world.playSound(clickedBlock.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F)
			waitFor(20)
			sign.setLine(2, "§aVenha jogar! ;)")
			sign.setLine(3, "§a(｡◕‿‿◕｡)")
			sign.update()
			e.player.world.playSound(clickedBlock.location, Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F)

			powerable.isPowered = false

			block.blockData = powerable

			for ((icon, payout) in prizes) {
				if (icon == value1 && icon == value2 && icon == value3) {
					player.sendMessage("${PREFIX} §aSortudo! Você conseguiu §2${payout} Sonecas§a! Que tal ir novamente? ʕ•ᴥ•ʔ")
					player.deposit(payout.toDouble(), TransactionContext(type = TransactionType.BETTING, extra = "no `cassino`"))
					player.world.spawnParticle(Particle.HAPPY_VILLAGER, position, 25, 0.5, 0.5, 0.5)
					return@schedule
				}
			}

			player.sendMessage("${PREFIX} §cQue pena, você não acertou nenhum... Que tal ir novamente? ʕ•ᴥ•ʔ")
			player.world.spawnParticle(Particle.ANGRY_VILLAGER, position, 25, 0.5, 0.5, 0.5)
		}
	}

	class CassinoCommand(val m: DreamCassino) : SparklyCommand(arrayOf("cassino")) {
		@Subcommand
		fun cassino(sender: Player) {
			if (sender.location.blacklistedTeleport) {
				sender.sendMessage("§eVocê está em um lugar que os nossos sistemas de GPS não conseguem te encontrar...")
				return
			}

			sender.teleport(m.config.spawn.toLocation())
			sender.sendMessage("${DreamCassino.PREFIX} Seja bem vindo ao Cassino!")
		}

		@Subcommand(["set_spawn"])
		@SubcommandPermission("dreamcassino.setup")
		fun setSpawn(sender: Player) {
			m.config.spawn = sender.location.toWrapper()

			val file = File(m.dataFolder, "config.json")
			file.writeText(DreamUtils.gson.toJson(m.config))

			sender.sendMessage("§eSpawn do cassino setado com sucesso!")
		}

	}

	class CassinoConfig(var spawn: LocationWrapper = LocationWrapper())
	class LocationWrapper(
		val world: String = "world",
		val x: Double = 0.toDouble(),
		val y: Double = 0.toDouble(),
		val z: Double = 0.toDouble(),
		val yaw: Float = 0.toFloat(),
		val pitch: Float = 0.toFloat()
	) {

		fun toLocation() = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
	}
}

fun Location.toWrapper() = DreamCassino.LocationWrapper(this.world.name, this.x, this.y, this.z, this.yaw, this.pitch)