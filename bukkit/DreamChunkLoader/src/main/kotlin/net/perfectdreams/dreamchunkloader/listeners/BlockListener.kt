package net.perfectdreams.dreamchunkloader.listeners

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.kyori.adventure.text.Component
import net.perfectdreams.dreamchunkloader.DreamChunkLoader
import net.perfectdreams.dreamchunkloader.data.ChunkLoaderBlockInfo
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcore.utils.rename
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Skull
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class BlockListener(private val m: DreamChunkLoader) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlace(e: BlockPlaceEvent) {
        if (e.itemInHand.itemMeta?.persistentDataContainer?.has(DreamChunkLoader.CHUNK_LOADER_INFO_KEY, PersistentDataType.STRING) == false)
            return

        // Is a chunk loader!
        e.player.sendMessage("Bloco de Chunk loader colocado com sucesso!")

        val skull = e.block.state as Skull
        skull.persistentDataContainer.set(
            DreamChunkLoader.CHUNK_LOADER_INFO_KEY,
            PersistentDataType.STRING,
            Json.encodeToString(
                ChunkLoaderBlockInfo(false, 0) // Copy power from chunk loader item
            )
        )
        skull.update()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        // Chun
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        val clickedBlock = e.clickedBlock ?: return
        val clickedBlockState = (clickedBlock.state as? Skull) ?: return

        // Not a chunk loader block!
        if (!clickedBlockState.persistentDataContainer.has(DreamChunkLoader.CHUNK_LOADER_INFO_KEY, PersistentDataType.STRING))
            return

        val claim = GriefPrevention.instance.dataStore.getClaimAt(clickedBlock.location, false, null)

        if (claim != null && (claim.ownerName != e.player.name && claim.allowContainers(e.player) != null)) {
            e.player.sendMessage("§cVocê não tem permissão para mexer neste item!")
            return
        }
        e.player.sendMessage("Opening inventory...")

        val chunkLoaderBlockInfo = Json.decodeFromString<ChunkLoaderBlockInfo>(
            clickedBlockState.persistentDataContainer
                .get(
                    DreamChunkLoader.CHUNK_LOADER_INFO_KEY,
                    PersistentDataType.STRING
                )!!
        )

        // TODO: Sound
        // TODO: Interactions
        // TODO: Save data
        e.player.playSound(clickedBlock.location, "sparklypower.sfx.electricity_arc", 1f, 1f)

        var requiredEnergy = 0
        for (x in 0 until 16) {
            for (y in clickedBlock.world.minHeight until clickedBlock.world.maxHeight) {
                for (z in 0 until 16) {
                    val block = clickedBlock.chunk.getBlock(x, y, z)
                    if (block.type == Material.NETHER_WART) {
                        requiredEnergy++
                    }
                }
            }
        }

        val inventory = createMenu(9, Component.text("Chunk Loader")) {
            slot(0, 0) {
                item = ItemStack(Material.GOLD_BLOCK)
                    .rename("Required Energy: $requiredEnergy")
            }
            slot(4, 0) {
                item = ItemStack(Material.DIAMOND)
                    .rename("Active: ${chunkLoaderBlockInfo.active}")

                onClick {
                    it.sendMessage("Clicou!")
                    it.closeInventory()

                    val newData =  Json.decodeFromString<ChunkLoaderBlockInfo>(
                        clickedBlockState.persistentDataContainer
                            .get(
                                DreamChunkLoader.CHUNK_LOADER_INFO_KEY,
                                PersistentDataType.STRING
                            )!!
                    ).let { it.copy(active = !it.active) }

                    clickedBlockState.persistentDataContainer
                        .set(
                            DreamChunkLoader.CHUNK_LOADER_INFO_KEY,
                            PersistentDataType.STRING,
                            Json.encodeToString(newData)
                        )

                    if (newData.active) {
                        // https://mineskin.org/88bdb85a09334ec1a10b0d3d38a1ded3
                        clickedBlock.chunk.addPluginChunkTicket(m)
                        clickedBlockState.setPlayerProfile(
                            Bukkit.createProfile(UUID(0, 0))
                                .apply {
                                    this.setProperty(
                                        ProfileProperty(
                                            "textures",
                                            "ewogICJ0aW1lc3RhbXAiIDogMTY0NDk2ODAxMDU4MCwKICAicHJvZmlsZUlkIiA6ICIzYTdhMDVjMDc0MTI0N2Q2YWVmMDMzMDNkOWNlMjMzNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcXJ0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E0YTU0MGRlOTc3OGE5NzMwZmYzOTdiMWE3YmQ3NzZhN2UyMzA5ODkzNDdmOTNiYjFlZTg4NjZiOGUwNzEzNzQiCiAgICB9CiAgfQp9"
                                        )
                                    )
                                }
                        )

                        it.world.playSound(clickedBlock.location, "sparklypower.sfx.positive_blip", 1f, 1f)
                    } else {
                        // https://mineskin.org/2ba4a7b63a2d48c898364a98e1f28b54
                        clickedBlock.chunk.removePluginChunkTicket(m)
                        clickedBlockState.setPlayerProfile(
                            Bukkit.createProfile(UUID(0, 0))
                                .apply {
                                    this.setProperty(
                                        ProfileProperty(
                                            "textures",
                                            "ewogICJ0aW1lc3RhbXAiIDogMTY0NDk2ODI5OTU1NSwKICAicHJvZmlsZUlkIiA6ICI4YjgyM2E1YmU0Njk0YjhiOTE0NmE5MWRhMjk4ZTViNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXBoaXRpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jY2Y5NjlmMDA4N2YyY2FkMTZkZDRiYWEwZTAxN2M5NDFiY2Y1NGFiMWQxZmRkNDE5Y2MwZmM2NTllNTg1MmNjIgogICAgfQogIH0KfQ=="
                                        )
                                    )
                                }
                        )
                    }

                    // Update the data
                    clickedBlockState.update()
                }
            }
        }.createInventory()

        e.player.openInventory(inventory)
    }
}