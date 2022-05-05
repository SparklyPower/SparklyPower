package net.perfectdreams.dreamlabirinto.events

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.Pair
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.pluralize
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamlabirinto.DreamLabirinto
import net.perfectdreams.dreamlabirinto.utils.MazeGenerator
import org.bukkit.*
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class EventoLabirinto(val plugin: DreamLabirinto) : ServerEvent("Labirinto", "/labirinto") {
    var wonPlayers = mutableListOf<UUID>()

    init {
        this.delayBetween = 3_600_000  // one hour
        this.requiredPlayers = 30 // 30 players
        this.discordAnnouncementRole = 539471142772146176L
    }

    var startLocation: Location? = null
    var startCooldown = 15
    val isPreStart: Boolean
        get() = startCooldown != 0

    fun join(player: Player) {
        val spawn = startLocation!!
        player.teleport(spawn)

        addLabirintoEffect(player, player.world, isPreStart)
        player.playSound(spawn, "perfectdreams.sfx.special_stage", SoundCategory.RECORDS, 1000f, 1f)
    }

    fun finish(winner: Player) {
        val world = Bukkit.getWorld("Labirinto")!!

        if (wonPlayers.size == 0)
            plugin.config.winner = winner.uniqueId

        // Player venceu a corrida!
        wonPlayers.add(winner.uniqueId)
        val howMuchMoneyWillBeGiven = 15_000 / wonPlayers.size
        val howMuchNightmaresWillBeGiven = if (wonPlayers.size == 1) 1 else 0

        winner.balance += howMuchMoneyWillBeGiven
        scheduler().schedule(plugin, SynchronizationContext.ASYNC) {
            if (wonPlayers.size == 1)
                DreamCore.INSTANCE.dreamEventManager.addEventVictory(
                    winner,
                    "Labirinto"
                )

            if (howMuchNightmaresWillBeGiven == 1)
                Cash.giveCash(winner, howMuchNightmaresWillBeGiven.toLong())
        }

        winner.fallDistance = 0.0f
        winner.fireTicks = 0
        PlayerUtils.healAndFeed(winner)

        winner.teleportToServerSpawn()

        if (howMuchNightmaresWillBeGiven == 1)
            Bukkit.broadcastMessage("${DreamLabirinto.PREFIX} §b${winner.displayName}§a venceu o labirinto em ${wonPlayers.size}º lugar! Ele ganhou §2$howMuchMoneyWillBeGiven sonecas§a e §c$howMuchNightmaresWillBeGiven pesadelo§a!")
        else
            Bukkit.broadcastMessage("${DreamLabirinto.PREFIX} §b${winner.displayName}§a venceu o labirinto em ${wonPlayers.size}º lugar! Ele ganhou §2$howMuchMoneyWillBeGiven sonecas§a!")

        if (wonPlayers.size == 3) { // Finalizar labirinto
            Bukkit.broadcastMessage("${DreamLabirinto.PREFIX} §eEvento Labirinto acabou, obrigado a todos que participaram! ^-^")

            world.players.forEach {
                it.fallDistance = 0.0f
                it.fireTicks = 0
                PlayerUtils.healAndFeed(it)

                it.teleportToServerSpawn()
            }

            running = false
            lastTime = System.currentTimeMillis()
            wonPlayers.clear()
        }

        return
    }

    override fun preStart() {
        running = true
        broadcastEventAnnouncement()
        start()
    }

    override fun start() {
        startCooldown = 15
        generateMaze()

        var idx = 0

        val world = Bukkit.getWorld("Labirinto")!!

        scheduler().schedule(plugin) {
            while (startCooldown > 0) {
                world.players.forEach {
                    it.sendTitle("§aLabirinto irá começar em...", "§c${startCooldown}s", 0, 100, 0)
                    it.playSound(it.location, Sound.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 1f, 1f)

                    addLabirintoEffect(it, it.world, true)
                }

                waitFor(20) // 1 segundo
                startCooldown--
            }

            world.players.forEach {
                it.sendTitle("§aCorra e se aventure!", "§bBoa sorte!", 0, 60, 20)
                it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)

                addLabirintoEffect(it, it.world, false)
            }
        }

        scheduler().schedule(plugin) {
            while (running) {
                // 10 minutes
                if (idx == 120) {
                    val extra = wonPlayers.size.let { if (it == 0) "ninguém conseguiu" else "só ${it.pluralize("pessoa pôde" to "pessoas puderam")}" }
                    Bukkit.broadcastMessage("§cPoxa, vida! Se passaram 10 minutos e $extra terminar o labirinto? Sinceramente, esperava bem mais...")

                    world.players.forEach {
                        it.fallDistance = 0.0f
                        it.fireTicks = 0
                        PlayerUtils.healAndFeed(it)

                        it.teleportToServerSpawn()
                    }

                    running = false
                    lastTime = System.currentTimeMillis()
                    wonPlayers.clear()
                    return@schedule
                }

                if (idx % 3 == 0) {
                    Bukkit.broadcastMessage("${DreamLabirinto.PREFIX} Evento Labirinto começou! §6/labirinto")
                }

                if (0 >= startCooldown)
                    world.players.forEach {
                        addLabirintoEffect(it, it.world, false)
                    }

                waitFor(100) // 5 segundos
                idx++
            }
        }
    }

    fun addLabirintoEffect(player: Player, world: World, isPreStart: Boolean) {
        player.fallDistance = 0.0f
        player.fireTicks = 0
        PlayerUtils.healAndFeed(player)

        player.activePotionEffects.filter {
            (
                    if (isPreStart)
                    { it.type != PotionEffectType.SLOW }
                    else
                    { it.type != PotionEffectType.SPEED && it.amplifier != 0 }
                    )
                    && (it.type != PotionEffectType.NIGHT_VISION)
                    && (it.type != PotionEffectType.INVISIBILITY) }
            .forEach { effect ->
                player.removePotionEffect(effect.type)
            }

        if (isPreStart) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 300, 1, true, false))
        } else {
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 0, false, false))
        }

        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false))

        broadcastFakeArmor(player, world)
    }

    fun broadcastFakeArmor(player: Player, world: World) {
        // Now we are going to fake send packets to everyone to remove all armor
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)

        // Uses the player's name as seed
        val random = SplittableRandom(player.name.hashCode().toLong())

        // Write the entity ID of the player...
        packet.integers.write(0, player.entityId)
        packet.slotStackPairLists.write(
            0,
            listOf(
                Pair(
                    EnumWrappers.ItemSlot.HEAD,
                    ItemStack(Material.AIR)
                ),
                Pair(
                    EnumWrappers.ItemSlot.CHEST,
                    ItemStack(Material.AIR)
                ),
                Pair(
                    EnumWrappers.ItemSlot.LEGS,
                    ItemStack(Material.AIR)
                ),
                Pair(
                    EnumWrappers.ItemSlot.FEET,
                    ItemStack(Material.LEATHER_BOOTS)
                        // Generate random color for the armor
                        .meta<LeatherArmorMeta> {
                            this.setColor(Color.fromRGB(
                                random.nextInt(0, 256), random.nextInt(0, 256), random.nextInt(0, 256)
                            ))
                        }
                ),
            )
        )

        // Now send the packet to everyone *except* the player!
        world.players.filter { it != player }.forEach {
            ProtocolLibrary.getProtocolManager().sendServerPacket(it, packet)
        }
    }

    fun generateMaze() {
        with(MazeGenerator(19, 19)) {
            generate(0, 0)

            var currentBlockY = 0
            var currentBlockX = 0

            val world = Bukkit.getWorld("Labirinto")

            // Start and End plates can be in different parts of the map each time
            val possiblePlates = mutableListOf(
                LabirintoPlate(
                    listOf(
                        Location(world, 1.0, 79.0, 1.0),
                        Location(world, 2.0, 79.0, 1.0),
                        Location(world, 1.0, 79.0, 2.0),
                        Location(world, 2.0, 79.0, 2.0)
                    ),
                    Location(world, 2.0, 80.0, 2.0, 270f, 0f)
                ),
                LabirintoPlate(
                    listOf(
                        Location(world, 56.0, 79.0, 56.0),
                        Location(world, 55.0, 79.0, 56.0),
                        Location(world, 56.0, 79.0, 55.0),
                        Location(world, 55.0, 79.0, 55.0)
                    ),
                    Location(world, 56.0, 80.0, 56.0, 90f, 0f)
                ),
                LabirintoPlate(
                    listOf(
                        Location(world, 1.0, 79.0, 56.0),
                        Location(world, 1.0, 79.0, 55.0),
                        Location(world, 2.0, 79.0, 56.0),
                        Location(world, 2.0, 79.0, 55.0)
                    ),
                    Location(world, 2.0, 80.0, 56.0, 180f, 0f)
                ),
                LabirintoPlate(
                    listOf(
                        Location(world, 56.0, 79.0, 2.0),
                        Location(world, 55.0, 79.0, 2.0),
                        Location(world, 56.0, 79.0, 1.0),
                        Location(world, 55.0, 79.0, 1.0)
                    ),
                    Location(world, 56.0, 80.0, 2.0, 0f, 0f)
                )
            )

            val startPlates = possiblePlates.random()
            // After getting the start plate, we remove the start plate from the list to avoid the end plate being in the same place
            possiblePlates.remove(startPlates)

            val endPlates = possiblePlates.random()

            val lines = this.displayToLines()

            val randomTextsForSigns = listOf(
                listOf(
                    "",
                    "Loritta é",
                    "§d§lfofa",
                    ""
                ),
                listOf(
                    "",
                    "Pantufa é",
                    "§d§lfofa",
                    ""
                ),
                listOf(
                    "",
                    "Gabriela é",
                    "§d§lfofa",
                    ""
                ),
                listOf(
                    "caminho errado",
                    "parça",
                    "rsrs",
                    ""
                ),
                listOf(
                    "§4§lDia 1:",
                    "Tentando",
                    "encontrar a",
                    "saída..."
                ),
                listOf(
                    "§4§lDia 4:",
                    "Ainda não",
                    "encontrei a",
                    "saída..."
                ),
                listOf(
                    "§4§lDia 8:",
                    "sdds de",
                    "respirar ar",
                    "fresco..."
                ),
                listOf(
                    "§4§lDia 10:",
                    "Passando fome",
                    "Será que o iFood",
                    "entrega aqui?"
                ),
                listOf(
                    "§4§lDia 14:",
                    "Queria sair",
                    "daqui de",
                    "dentro..."
                ),
                listOf(
                    "§4§lDia 20:",
                    "Talvez esteja",
                    "ficando doidin",
                    ""
                ),
                listOf(
                    "§4§lDia 30:",
                    "Saudades daquilo",
                    "que a gente",
                    "não viveu"
                ),
                listOf(
                    "§4§lDia 45:",
                    "...",
                    "não tem saída"
                ),
                listOf(
                    "Será que aqui é",
                    "o caminho certo?",
                    "Hmmmm"
                ),
                listOf(
                    "Acho que aqui é",
                    "o caminho errado"
                ),
                listOf(
                    "Talvez estas",
                    "placas sejam",
                    "apenas lorotas"
                ),
                listOf(
                    "Neymar",
                    "Junior"
                ),
                listOf(
                    "Quarentena",
                    "no Labirinto!!"
                ),
                listOf(
                    "emojo"
                ),
                listOf(
                    "Algum dia você",
                    "irá encontrar",
                    "a saída, né?"
                )
            )

            lines.forEachIndexed { y, line ->
                // Choose the type for each line
                line.forEachIndexed { x, char ->
                    val blockLocation = Location(world, x.toDouble(), 80.0, currentBlockY.toDouble())

                    val distance = blockLocation.distanceSquared(endPlates.startingPosition)

                    val floorType = when {
                        256 >= distance -> Material.GREEN_STAINED_GLASS
                        676 >= distance -> Material.YELLOW_STAINED_GLASS
                        1296 >= distance -> Material.ORANGE_STAINED_GLASS
                        2116 >= distance -> Material.GRAY_STAINED_GLASS
                        else -> Material.BLACK_STAINED_GLASS
                    }

                    val wallType = when {
                        256 >= distance -> Material.GREEN_CONCRETE
                        676 >= distance -> Material.YELLOW_CONCRETE
                        1296 >= distance -> Material.ORANGE_CONCRETE
                        2116 >= distance -> Material.GRAY_CONCRETE
                        else -> Material.BLACK_CONCRETE
                    }

                    Location(world, x.toDouble(), 79.0, currentBlockY.toDouble()).block.type = floorType
                    Location(world, x.toDouble(), 83.0, currentBlockY.toDouble()).block.type = floorType

                    repeat(5) {
                        val blockLocation = Location(world, x.toDouble(), 79.0 + it, currentBlockY.toDouble())
                        if (char == '+' || char == '-' || char == '|') {
                            blockLocation.block.type = wallType
                        } else {
                            if (it == 2 && chance(1.0)) {
                                val blocc = BlockUtils.attachWallSignAt(blockLocation)

                                if (blocc != null) {
                                    val blockState = blocc.state

                                    if (blockState is Sign) {
                                        randomTextsForSigns.random().forEachIndexed { index, s ->
                                            blockState.setLine(index, s)
                                        }
                                        blockState.update()
                                    }
                                }
                            } else {
                                blockLocation.block.type = if (it == 0 || it == 4) floorType else Material.AIR
                            }
                        }
                    }
                }
                currentBlockY++
            }

            startLocation = startPlates.startingPosition

            // Replace start plates floor with the proper block

            possiblePlates.forEach {
                it.blocks.forEach {
                    it.block.type = Material.SEA_LANTERN
                }
            }

            startPlates.blocks.forEach {
                it.block.type = Material.DIAMOND_BLOCK
            }

            endPlates.blocks.forEach {
                it.block.type = Material.EMERALD_BLOCK
            }
        }
    }

    class LabirintoPlate(
        val blocks: List<Location>,
        val startingPosition: Location
    )
}