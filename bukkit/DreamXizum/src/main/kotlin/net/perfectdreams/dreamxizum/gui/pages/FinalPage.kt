package net.perfectdreams.dreamxizum.gui.pages

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.world.entity.EquipmentSlot
import net.perfectdreams.dreamcore.utils.DreamNPC
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamcore.utils.stripColors
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.pages.betting.BettingPage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class FinalPage(val player: Player) : AbstractPage() {
    private val options = player.battle!!.options

    companion object {
        private val clocks = mutableListOf<ItemStack>()

        val minutes = listOf(3, 5, 7, 10)
        val timeColors = listOf("§x§f§f§9§c§4§0§l", "§x§f§f§8§8§7§4§l", "§x§f§f§7§2§9§7§l", "§x§f§f§5§5§b§4§l")
        val pvpColors = listOf("§x§f§7§d§0§3§1§l", "§x§d§c§4§5§8§6§l")

        init {
            // Thanks to https://www.spigotmc.org/threads/tutorial-player-skull-with-custom-skin.143323/
            with (XizumConfig.models.clocks) { listOf(threePM, fivePM, sevenPM, tenPM) }.forEach {
                Material.PLAYER_HEAD.toItemStack().apply {
                    val meta = itemMeta as SkullMeta
                    val profile = GameProfile(UUID.randomUUID(), null).apply {
                        properties.put("textures", Property("textures", it.texture))
                    }

                    try {
                        with (meta.javaClass) {
                            getDeclaredField("profile").apply {
                                isAccessible = true
                                set(meta, profile)
                            }

                            getDeclaredField("serializedProfile").apply {
                                isAccessible = true
                                set(meta, type.getDeclaredConstructor().newInstance())
                            }
                        }
                    } catch (exception: Exception) { exception.printStackTrace() }

                    itemMeta = meta
                    clocks.add(this)
                }
            }
        }
    }

    init {
        nextPage = BettingPage()

        with (npcs) { listOf(allowMcMMO, dropHeads) }.forEach { model ->
            toggleableButton(model) { _, toggled ->
                if (model == npcs.dropHeads) options.dropHeads = toggled
                else options.allowMcMMO = toggled
            }.npc.apply {
                if (model == npcs.dropHeads) changeItem(
                    Material.PLAYER_HEAD.toItemStack().meta<SkullMeta> {
                        playerProfile = player.playerProfile
                    }, EquipmentSlot.HEAD
                )
            }
        }

        with (npcs) { listOf(battleTime, pvpVersion) }.forEach { model ->
            lateinit var dreamNPC: DreamNPC
            var totalClicks = 0

            button(model) {
                lateinit var text: String
                totalClicks++

                if (model == npcs.battleTime)
                    with (totalClicks % 4) {
                        text = "${timeColors[this]}${minutes[this]} minutos"
                        options.timeLimit = minutes[this]
                        dreamNPC.lines = dreamNPC.lines.mapTo(mutableListOf()) { timeColors[this] + it.stripColors() }.apply { set(lastIndex, text) }
                        dreamNPC.changeItem(clocks[this], EquipmentSlot.HEAD)
                    }

                else
                    with (totalClicks % 2) {
                        text = pvpColors[this] + if (this == 0) "1.8" else "1.16+"
                        options.legacyPvp = this == 0
                        dreamNPC.lines = dreamNPC.lines.mapTo(mutableListOf()) { pvpColors[this] + it.stripColors() }.apply { set(lastIndex, text) }
                    }
            }.apply {
                with (npc) {
                    if (model == npcs.battleTime) changeItem(clocks[0], EquipmentSlot.HEAD)
                    dreamNPC = this
                }
                enableCooldown = false
            }
        }
    }

    override fun onReturn() {
        nextPage = BettingPage()
        options.sonecas = 0.0
        options.cash = 0L
    }
}