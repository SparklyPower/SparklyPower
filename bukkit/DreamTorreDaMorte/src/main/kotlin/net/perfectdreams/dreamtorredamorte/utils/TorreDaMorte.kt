package net.perfectdreams.dreamtorredamorte.utils

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.removeAllPotionEffects
import net.perfectdreams.dreamcorreios.utils.addItemIfPossibleOrAddToPlayerMailbox
import net.perfectdreams.dreammapwatermarker.DreamMapWatermarker
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.*

class TorreDaMorte(val m: DreamTorreDaMorte) {
    var spawns = mutableListOf<Location>()
    var players = mutableSetOf<Player>()
    var playersInQueue = mutableSetOf<Player>()
    var storedPlayerInventory = mutableMapOf<Player, Array<ItemStack?>>()
    var canAttack = false
    var isStarted = false
    var isPreStart = false
    val isGamePhase
        get() = isStarted && !isPreStart
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
    var isServerEvent = false
    var currentEventId = UUID.randomUUID()
    var lastWinner = UUID.randomUUID()

    fun preStart(isServerEvent: Boolean) {
        this.isServerEvent = isServerEvent
        if (isServerEvent)
            m.eventoTorreDaMorte.running = true

        val eventId = UUID.randomUUID()
        this.currentEventId = eventId

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
            val startAt = if (isServerEvent) 60 else 30

            for (i in startAt downTo 1) {
                if (currentEventId != eventId) // Parece que o evento acabou e outro começou
                    return@schedule

                val announce = (i in 15..60 && i % 15 == 0) || (i in 0..14 && i % 5 == 0)

                if (announce) {
                    if (isServerEvent) {
                        Bukkit.broadcastMessage("${DreamTorreDaMorte.PREFIX} O Evento Torre da Morte começará em $i segundos! Use §6/torre§e para entrar! (Guarde os itens antes de entrar, vai se dá problema)")
                    } else {
                        Bukkit.broadcastMessage("${DreamTorreDaMorte.PREFIX} A Torre da Morte *valendo nada* começará em $i segundos! Use §6/torre minigame§e para entrar! (Guarde os itens antes de entrar, vai se dá problema)")
                    }
                }

                waitFor(20)
            }

            if (currentEventId != eventId) // Parece que o evento acabou e outro começou
                return@schedule

            start()
        }
    }

    fun start() {
        isPreStart = false

        val validPlayersInQueue = playersInQueue.filter { it.isValid && it.world.name == "TorreDaMorte" }
        playersInQueue.clear()
        playersInQueue.addAll(validPlayersInQueue)

        if (1 >= playersInQueue.size) {
            if (isServerEvent)
                m.eventoTorreDaMorte.running = false

            isStarted = false
            isPreStart = false

            playersInQueue.forEach { player ->
                player.teleport(DreamCore.dreamConfig.getSpawn())
                player.sendMessage("${DreamTorreDaMorte.PREFIX} §cA Torre da Morte foi cancelada devido a falta de players...")
            }

            playersInQueue.clear()
            return
        }

        playersInQueue.forEachIndexed { index, player ->
            val locationToTeleportTo = spawns[index % spawns.size]

            PlayerUtils.healAndFeed(player)
            player.removeAllPotionEffects()
            players.add(player)

            storedPlayerInventory[player] = player.inventory.contents!!.clone()
            player.openInventory.close()
            player.inventory.clear()

            val successfullyTeleported = player.teleport(locationToTeleportTo)
            if (!successfullyTeleported) {
                // uuh... that's not good
                // we will skip the finish check because what if it was the first user that caused this issue?
                removeFromGame(player, skipFinishCheck = true)
                return@forEachIndexed
            }

            player.inventory.addItem(
                ItemStack(Material.STICK)
                    .rename("§c§lO Poder do Vieirinha")
                    .apply {
                        this.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1)
                        this.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2)
                    }
            )
        }

        // Iniciar minigame... daqui a pouquitcho, yay!
        scheduler().schedule(m) {
            players.forEach {
                it.sendTitle("§c5", "§f", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.2f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c4", "§f", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.4f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c3", "§f", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.6f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c2", "§f", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.8f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§c1", "§f", 0, 20, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            }
            waitFor(20)
            players.forEach {
                it.sendTitle("§4§lLutem!", "§f", 0, 20, 0)
                it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                it.playSound(it.location, "perfectdreams.sfx.special_stage", 1f, 1f)
            }

            canAttack = true

            while (isStarted) {
                if (players.size == 1) {
                    m.logger.warning { "Players remaining detected as 1 in the event in the repeating schedule! This should never happen!" }
                    finish()
                    continue
                }

                m.logger.info { "Remaining players: ${players.map { it.name }}" }
                // A cada um segundo iremos verificar players inválidos que ainda estão no jogo
                // We will also check if the player y position is above the queue spawn because, if it is, then they weren't teleported (for some reason)
                val invalidPlayers = players.filter { !it.isValid || it.location.world.name != "TorreDaMorte" || (it.location.world.name == "TorreDaMorte" && it.location.y >= queueSpawn.y) }
                m.logger.info { "Removing invalid players $invalidPlayers" }
                invalidPlayers.forEach {
                    removeFromGame(it, skipFinishCheck = true)
                }

                waitFor(20L)
            }
        }
    }

    fun finish() {
        val player = players.firstOrNull()
        if (player == null) {
            isStarted = false
            isPreStart = false
            storedPlayerInventory.clear()
            lastHits.clear()

            m.eventoTorreDaMorte.lastTime = System.currentTimeMillis()
            m.eventoTorreDaMorte.running = false

            Bukkit.broadcastMessage("${DreamTorreDaMorte.PREFIX} Parece a Torre da Morte acabou sem nenhum ganhador... isto é um bug e jamais deveria acontecer!")
            return
        }

        // No need to check if the event has finished for the last player
        removeFromGame(player, skipFinishCheck = true)

        isStarted = false
        isPreStart = false
        storedPlayerInventory.clear()
        lastHits.clear()

        if (isServerEvent) {
            m.eventoTorreDaMorte.lastTime = System.currentTimeMillis()
            m.eventoTorreDaMorte.running = false

            val howMuchMoneyWillBeGiven = 15_000
            val howMuchNightmaresWillBeGiven = 1

            Bukkit.broadcastMessage("${DreamTorreDaMorte.PREFIX} §b${player.displayName}§e venceu a Torre da Morte! Ele ganhou §2$howMuchMoneyWillBeGiven sonecas§a e §c$howMuchNightmaresWillBeGiven pesadelo§a!")

            lastWinner = player.uniqueId
            player.deposit(howMuchMoneyWillBeGiven.toDouble(), TransactionContext(type = TransactionType.EVENTS, extra = "Torre da Morte"))

            val map = ItemStack(Material.FILLED_MAP).meta<MapMeta> {
                this.mapId = 25336

                this.displayName(
                    Component.text("Venci o evento ")
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("Torre da Morte").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD))
                        .append(Component.text("!"))
                )
            }

            DreamMapWatermarker.watermarkMap(map, null)
            player.addItemIfPossibleOrAddToPlayerMailbox(map)

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                val wonAt = System.currentTimeMillis()

                DreamCore.INSTANCE.dreamEventManager.addEventVictory(
                    player,
                    "Torre da Morte",
                    wonAt
                )

                Cash.giveCash(player, howMuchNightmaresWillBeGiven.toLong(), TransactionContext(type = TransactionType.EVENTS, extra = "Torre da Morte"))
            }
        } else {
            Bukkit.broadcastMessage("${DreamTorreDaMorte.PREFIX} §b${player.displayName}§e venceu o Minigame da Torre da Morte! Parabéns!")
        }
    }

    fun removeFromGame(player: Player, skipFinishCheck: Boolean) {
        m.logger.info { "Removing ${player.name} from the game. Skip finish check? $skipFinishCheck" }
        if (!players.contains(player))
            return

        // Você perdeu, que sad...
        players.remove(player)

        // Reset player velocity to avoid them dying before teleporting (due to falling from the tower)
        player.velocity = Vector(0, 0, 0)
        player.teleport(DreamCore.dreamConfig.getSpawn())

        // Restaurar o inventário do player
        val storedInventory = storedPlayerInventory[player]
        if (storedInventory != null)
            player.inventory.setContents(storedInventory.filterNotNull().toTypedArray())

        val killer = lastHits[player]
        m.logger.info { "${killer?.name} killed ${player.name}!" }
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

        // Remove the player from the last hits map
        lastHits.remove(player)

        if (!skipFinishCheck && isGamePhase && players.size == 1)
            finish()
    }

    fun removeFromQueue(player: Player) {
        m.logger.info { "Removing ${player.name} from the queue" }
        if (!playersInQueue.contains(player))
            return

        player.teleport(DreamCore.dreamConfig.getSpawn())
        playersInQueue.remove(player)
    }

    fun joinQueue(player: Player) {
        m.logger.info { "Adding ${player.name} to the queue!" }
        player.teleport(queueSpawn)
        playersInQueue.add(player)
    }
}