package net.perfectdreams.dreamcorrida.events

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.Pair
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.PlayerUtils
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcorrida.DreamCorrida
import net.perfectdreams.dreamcorrida.utils.Checkpoint
import net.perfectdreams.dreamcorrida.utils.Corrida
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class EventoCorrida(val m: DreamCorrida) : ServerEvent("Corrida", "/corrida") {
    init {
        this.requiredPlayers = 40
        this.delayBetween = 3600000 // 1 hora
        this.discordAnnouncementRole = 477979984275701760L
    }

    var corrida: Corrida? = null
    var playerCheckpoints = mutableMapOf<Player, Checkpoint>()
    var wonPlayers = mutableListOf<UUID>()
    var startCooldown = 15
    val isPreStart: Boolean
        get() = startCooldown != 0
    val damageCooldown = mutableMapOf<Player, Long>()

    override fun preStart() {
        val canStart = m.availableCorridas.filter { it.ready }.isNotEmpty()

        if (!canStart) {
            this.lastTime = System.currentTimeMillis()
            return
        }

        val corrida = m.availableCorridas.filter { it.ready }.random()

        preStart(corrida)
    }

    fun preStart(corrida: Corrida) {
        running = true

        this.corrida = corrida

        broadcastEventAnnouncement()
        start()
    }

    override fun start() {
        startCooldown = 15
        damageCooldown.clear()

        // If it is null then F, but this should never be null at this point!
        val corrida = corrida!!
        val spawnPoint = corrida.spawn.toLocation()

        val world = spawnPoint.world

        scheduler().schedule(m) {
            while (startCooldown > 0) {
                world.players.forEach {
                    it.sendTitle("§aCorrida irá começar em...", "§c${startCooldown}s", 0, 100, 0)
                    it.playSound(it.location, Sound.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 1f, 1f)

                    addCorridaEffect(it, it.world, true)
                }

                waitFor(20) // 1 segundo
                startCooldown--
            }

            world.players.forEach {
                it.sendTitle("§aCorra e se divirta!", "§bBoa sorte!", 0, 60, 20)
                it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)

                addCorridaEffect(it, world, false)
            }
        }

        var idx = 0
        scheduler().schedule(m) {
            while (running) {
                if (idx % 3 == 0) {
                    Bukkit.broadcastMessage("${DreamCorrida.PREFIX} Evento Corrida começou! §6/corrida")
                }

                if (0 >= startCooldown)
                    world.players.forEach {
                        addCorridaEffect(it, world, false)
                    }

                waitFor(100) // 5 segundos
                idx++
            }
        }
    }

    fun addCorridaEffect(player: Player, world: World, isPreStart: Boolean) {
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
                    && (it.type != PotionEffectType.JUMP && it.amplifier != 0)
                    && (it.type != PotionEffectType.NIGHT_VISION)
                    && (it.type != PotionEffectType.INVISIBILITY)
        }.forEach { effect ->
            player.removePotionEffect(effect.type)
        }

        if (isPreStart) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 300, 1, true, false))
        } else {
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 1_200, 0, false, false))
        }
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 1_200, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 1_200, 0, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1_200, 0, false, false))

        broadcastFakeArmor(player, world)
        sendEmptyInventory(player)
    }

    fun sendEmptyInventory(player: Player) {
        // We are going to empty the player's inventory with packets
        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WINDOW_ITEMS)

        val air = ItemStack(Material.AIR)

        packet.integers.write(0, 0) // 0 = Player Inventory
        packet.itemListModifier.write(0, (0..44).map { air }) // No items should be used

        // Send packet to player
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
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
}
