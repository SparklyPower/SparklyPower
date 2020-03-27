package net.perfectdreams.dreamtorredamorte.utils

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.InstantFirework
import net.perfectdreams.dreamcore.utils.PlayerUtils
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class TorreDaMorte(val m: DreamTorreDaMorte) {
    var spawns = mutableListOf<Location>()
    var players = mutableListOf<Player>()
    var playersInQueue = mutableListOf<Player>()
    var storedPlayerInventory = mutableMapOf<Player, Array<ItemStack>>()
    var canAttack = false
    var isStarted = false
    var isPreStart = false
    val world by lazy { Bukkit.getWorld("TorreDaMorte") }
    val queueSpawn by lazy {
        Location(
            world,
            10.5,
            140.0,
            0.5,
            0.0f,
            0.0f
        )
    }
    val lastHits = mutableMapOf<Player, Player>()

    fun preStart() {
        spawns.clear()
        spawns.addAll(
            listOf(
                Location(
                    world,
                    10.5,
                    127.0,
                    0.5,
                    0.0f,
                    0.0f
                )
            )
        )
        lastHits.clear()

        playersInQueue.clear()
        isStarted = true
        isPreStart = true
        canAttack = false

        scheduler().schedule(m) {
            Bukkit.broadcastMessage("${DreamTorreDaMorte.PREFIX} §eA Torre da Morte irá iniciar em 30 segundos! (GUARDE OS ITENS ANTES DE ENTRAR VAI SE DÁ RUIM) §6/torre")
            waitFor(30 * 20)
            Bukkit.broadcastMessage("Torre Teste 2")

            start()
        }
    }

    fun start() {
        val validPlayersInQueue = playersInQueue.filter { it.world.name == "TorreDaMorte" }
        playersInQueue.clear()
        playersInQueue.addAll(validPlayersInQueue)

        if (1 >= playersInQueue.size) {
            isStarted = false
            isPreStart = false

            playersInQueue.forEach { player ->
                player.teleport(DreamCore.dreamConfig.spawn)
                player.sendMessage("${DreamTorreDaMorte.PREFIX} §cA Torre da Morte foi cancelada devido a falta de players...")
            }

            playersInQueue.clear()
            return
        }

        playersInQueue.forEachIndexed { index, player ->
            val locationToTeleportTo = spawns[index % spawns.size]

            player.teleport(locationToTeleportTo)
            PlayerUtils.healAndFeed(player)
            player.activePotionEffects.forEach {
                player.removePotionEffect(it.type)
            }
            players.add(player)

            storedPlayerInventory[player] = player.inventory.contents.clone()
            player.openInventory.close()
            player.inventory.clear()
        }

        isPreStart = false

        // Iniciar minigame... daqui a pouquitcho, yay!

        scheduler().schedule(m) {
            players.forEach {
                it.sendTitle("§c5", "", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.2f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c4", "", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.4f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c3", "", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.6f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c2", "", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.8f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c1", "", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§4§lLutem!", "", 0, 20, 0)
                it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                it.playSound(it.location, "perfectdreams.sfx.special_stage", 1f, 1f)
            }

            canAttack = true
        }
    }

    fun finish() {
        val player = players.first()
        removeFromGame(player)

        isStarted = false
        isPreStart = false
        storedPlayerInventory.clear()
        lastHits.clear()

        Bukkit.broadcastMessage("${DreamTorreDaMorte.PREFIX} §b${player.displayName}§e venceu a Torre da Morte!")
    }

    fun removeFromGame(player: Player) {
        if (!players.contains(player))
            return

        // Você perdeu, que sad...
        players.remove(player)

        player.teleport(DreamCore.dreamConfig.spawn)

        // Restaurar o inventário do player
        val storedInventory = storedPlayerInventory[player]
        if (storedInventory != null)
            player.inventory.contents = storedInventory

        val killer = lastHits[player]
        if (killer != null && players.contains(killer)) {
            killer.sendMessage("${DreamTorreDaMorte.PREFIX} §aVocê matou §b${player.displayName}§a! Como recompensa, você teve a sua vida recuperada e outras coisas legais!")
            PlayerUtils.healAndFeed(killer)

            killer.addPotionEffect(
                PotionEffect(
                    PotionEffectType.SPEED,
                    20 * 5,
                    0
                )
            )

            InstantFirework.spawn(
                killer.location,
                FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.RED)
                    .withFade(Color.BLACK)
                    .build()
            )
        }

        if (players.size == 1)
            finish()
    }

    fun removeFromQueue(player: Player) {
        if (!playersInQueue.contains(player))
            return

        player.teleport(DreamCore.dreamConfig.spawn)
        playersInQueue.remove(player)
    }

    fun joinQueue(player: Player) {
        player.teleport(queueSpawn)
        playersInQueue.add(player)
    }
}