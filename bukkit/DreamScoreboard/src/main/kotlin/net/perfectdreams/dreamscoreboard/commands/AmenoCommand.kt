package net.perfectdreams.dreamscoreboard.commands

import com.comphenix.packetwrapper.WrapperPlayServerSetSlot
import com.comphenix.packetwrapper.WrapperPlayServerWindowItems
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.md_5.bungee.api.ChatColor
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.Bukkit
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Skull
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_18_R1.scoreboard.CraftScoreboard
import org.bukkit.craftbukkit.v1_18_R1.scoreboard.CraftScoreboardManager
import org.bukkit.craftbukkit.v1_18_R1.util.WeakCollection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

object AmenoCommand : DSLCommandBase<DreamScoreboard> {
    val KEY = NamespacedKey("test123", "hello")

    override fun command(plugin: DreamScoreboard) = create(listOf("ameno")) {
        permission = "dreamscoreboard.ameno"

        executes {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            // woo more stuff
                            val block = player.getTargetBlock(10)!!

                            if (this.args[0] == "check") {
                                val skullState = block.state as Skull
                                val storedString = skullState.persistentDataContainer.get(KEY, PersistentDataType.STRING)

                                player.sendMessage("Stored String: $storedString")
                            } else if (this.args[0] == "place") {
                                block.type = Material.PLAYER_HEAD
                                val skullState = block.state as Skull
                                skullState.persistentDataContainer
                                    .set(KEY, PersistentDataType.STRING, "Hello World!!")
                                skullState.update()
                            }
                            return@executes
                        }

                        // We are going to empty the player's inventory with packets
                        val packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WINDOW_ITEMS)

                        val air = ItemStack(Material.AIR)

                        packet.integers.write(0, 0) // 0 = Player Inventory
                        packet.itemListModifier.write(0, (0..44).map { air }) // No items should be used

                        // Send packet to player
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet)
                        return@executes
                    }

                    val wpsss = WrapperPlayServerSetSlot()
                    wpsss.slot = 45
                    wpsss.windowId = 0
                    wpsss.slotData = ItemStack(Material.TOTEM_OF_UNDYING).meta<ItemMeta> {
                        setCustomModelData(1)
                    }
                    wpsss.sendPacket(player)
                    player.playEffect(EntityEffect.TOTEM_RESURRECT)
                    player.updateInventory()
                    return@executes
                }

                val playerScoresMethod = (Bukkit.getScoreboardManager() as CraftScoreboardManager)::class.java.getDeclaredField("scoreboards")
                playerScoresMethod.isAccessible = true
                val map = playerScoresMethod.get(Bukkit.getScoreboardManager())
                println(map as WeakCollection<CraftScoreboard>)
                println(map.size)

                val playerBoardsMethod = (Bukkit.getScoreboardManager() as CraftScoreboardManager)::class.java.getDeclaredField("playerBoards")
                playerBoardsMethod.isAccessible = true
                val map2 = playerBoardsMethod.get(Bukkit.getScoreboardManager())
                println(map2 as Map<CraftPlayer, CraftScoreboard>)
                println(map2.size)

                return@executes
            }

            plugin.schedule(SynchronizationContext.ASYNC) {
                val image = ImageIO.read(File(plugin.dataFolder, args[0]))

                for (y in 0 until image.height) {
                    val strBuilder = StringBuilder()

                    for (x in 0 until image.width) {
                        strBuilder.append("${ChatColor.of(Color(image.getRGB(x, y)))}⬛")
                    }

                    switchContext(SynchronizationContext.SYNC)

                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(strBuilder.toString())
                    }

                    switchContext(SynchronizationContext.ASYNC)
                }
            }
        }
    }
}