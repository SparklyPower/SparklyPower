package net.perfectdreams.dreamdropparty.events

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.md_5.bungee.api.ChatColor
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamdropparty.DreamDropParty
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import java.awt.Color

class DropParty(val m: DreamDropParty) : ServerEvent("Festa da Firma", "/dropparty") {
    init {
        this.requiredPlayers = 60
        this.delayBetween = 3600000 // 1 hour
        this.discordAnnouncementRole = "539979402143072267"
    }

    override fun getWarmUpAnnouncementMessage(idx: Int): Any {
        return "${DreamDropParty.PREFIX} Se preparem que o evento ${getEventTitleRainbowColored(0.0f)}§e começará em $idx segundos! §6/dropparty"
    }

    override fun preStart() {
        running = true

        countdown()
    }

    override fun start() {
        val world = Bukkit.getWorld(m.config.spawn.world)!!

        Bukkit.broadcastMessage("${DreamDropParty.PREFIX} O evento ${getEventTitleRainbowColored(0.0f)}§e começou! §6/dropparty")

        val color = BarColor.values().random()
        val bossBar = Bukkit.createBossBar(getEventTitleRainbowColored(0.0f), color, BarStyle.SEGMENTED_20)

        val centerOfDropPartyWorld = Location(world, 0.0, 83.0, 0.0)

        world.players.forEach {
            it.playSound(centerOfDropPartyWorld, "minecraft:music_disc.11", 1000.0f, 1f)
        }

        scheduler().schedule(m) {
            // We sync it according to the music_disc 11 beat (funk do yudi)
            // the first drop is at 8.30s after the song started
            waitFor(166)

            repeat(26) {
                val barColor = BarColor.values().random()
                bossBar.color = barColor
                bossBar.setTitle(getEventTitleRainbowColored(it.toFloat() / 15))

                world.players.forEach {
                    bossBar.addPlayer(it)
                }

                switchContext(SynchronizationContext.SYNC)
                playerLoop@for (player in world.players) {
                    for (itemWithChance in m.prizes) {
                        val chance = itemWithChance.chance

                        if (chance(chance)) {
                            val itemStack = itemWithChance.itemStack.clone()

                            if (itemWithChance.randomEnchant) {
                                Enchantment.values()
                                    .filter { it.canEnchantItem(itemStack) }
                                    .forEach {
                                        if (chance(25 * chance)) {
                                            itemStack.addEnchantment(it, DreamUtils.random.nextInt(1, it.maxLevel + 1))
                                        }
                                    }
                            }

                            // Get a random location around the player
                            val dropLocation = player.location
                                .add(
                                    DreamUtils.random.nextDouble(-4.0, 5.0),
                                    4.0,
                                    DreamUtils.random.nextDouble(-4.0, 5.0)
                                )

                            dropLocation.world.dropItem(dropLocation, itemStack)

                            dropLocation.world.spawnParticle(Particle.VILLAGER_HAPPY, dropLocation, 3, 1.0, 1.0, 1.0)
                            // If a item drops for the current player, move to the next player
                            continue@playerLoop
                        }
                    }
                }

                switchContext(SynchronizationContext.ASYNC)

                bossBar.progress = Math.max(0.0, 1.0 - (it.toFloat() / 26))
                // Every "oh" beat is 550ms between each other
                // so 11 ticks

                // We want it to repeat until the first part of the song ends, so we need to repeat it 26 times
                waitFor(9)
            }

            bossBar.removeAll()
            Bukkit.broadcastMessage("${DreamDropParty.PREFIX} Evento ${getEventTitleRainbowColored(0.0f)}§e acabou! Obrigado a todos que participaram!")

            running = false
            lastTime = System.currentTimeMillis()

            waitFor(400)

            switchContext(SynchronizationContext.SYNC)

            world.players.forEach {
                it.teleport(DreamCore.dreamConfig.getSpawn())
            }

            world.entities.filter { it.type == EntityType.DROPPED_ITEM }.forEach {
                it.remove()
            }
        }
    }

    private fun getEventTitleRainbowColored(shiftHueBy: Float): String {
        val firstColor = Color(252, 3, 3)

        val rgbToHsb = Color.RGBtoHSB(firstColor.red, firstColor.green, firstColor.blue, null)

        val text = DreamDropParty.FESTA_DA_FIRMA
        val output = StringBuilder()

        for ((index, ch) in text.withIndex()) {
            // val hue = rgbToHsb[0]
            val saturation = rgbToHsb[1]
            val brightness = rgbToHsb[2]

            val color2 = Color.getHSBColor((index.toFloat() / text.length) + shiftHueBy, saturation, brightness)
            if (!ch.isWhitespace()) {
                output.append(ChatColor.of(color2))
                output.append("§l")
            }

            output.append(ch)
        }

        return output.toString()
    }
}
