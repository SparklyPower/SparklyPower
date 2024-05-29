package net.perfectdreams.dreammapwatermarker.loricoolcards

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammapwatermarker.DreamMapWatermarker
import net.perfectdreams.dreammapwatermarker.map.ImgRenderer
import net.perfectdreams.dreammapwatermarker.tables.LoriCoolCardsGeneratedMaps
import net.perfectdreams.exposedpowerutils.sql.transaction
import org.bukkit.Bukkit
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.jetbrains.exposed.sql.insert
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.time.Instant
import javax.imageio.ImageIO

class LoriCoolCardsHandler(val m: DreamMapWatermarker) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun startLoriCoolCardsMapGenerator() {
        m.launchAsyncThread {
            while (true) {
                try {
                    logger.info { "Retrieving LoriCoolCards' albums..." }
                    val albumsResponse = DreamUtils.http.get("${m.config.lorittaInternalApiUrl.removeSuffix("/")}/sparklypower/loricoolcards/albums")

                    val albums = Json.decodeFromString<List<LoriCoolCardsAlbum>>(albumsResponse.bodyAsText())

                    // Technically all stickers should have unique IDs
                    val generatedMaps = transaction(Dispatchers.IO, Databases.databaseNetwork) {
                        LoriCoolCardsGeneratedMaps.select(LoriCoolCardsGeneratedMaps.sticker)
                            .toList()
                            .map { it[LoriCoolCardsGeneratedMaps.sticker] }
                            .toSet()
                    }

                    logger.info { "Currently generated sticker maps: ${generatedMaps.size}" }

                    for (album in albums) {
                        try {
                            logger.info { "Retrieving LoriCoolCards' album ${album.id} (${album.eventName}) stickers..." }

                            val stickersResponse = DreamUtils.http.get("${m.config.lorittaInternalApiUrl.removeSuffix("/")}/sparklypower/loricoolcards/albums/${album.id}/stickers")

                            val stickers = Json.decodeFromString<List<LoriCoolCardsSticker>>(stickersResponse.bodyAsText())
                                .sortedBy { it.fancyCardId }

                            val stickersToBeGenerated = stickers.filter { it.id !in generatedMaps }

                            logger.info { "LoriCoolCards' album ${album.id} (${album.eventName}) has ${stickers.size} stickers! From these ${stickers.size} stickers, we need to generate ${stickersToBeGenerated.size} stickers!" }

                            for (sticker in stickers.filter { it.id !in generatedMaps }) {
                                try {
                                    logger.info { "Downloading sticker ${sticker.id} (${sticker.fancyCardId} - ${sticker.title}) from album ${album.id} (${album.eventName})..." }

                                    // Download image
                                    val image = ImageIO.read(URL(sticker.cardFrontImageUrl))

                                    // Scale down image
                                    val mapImage = m.toBufferedImage(image.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH))

                                    val baseMapImage = BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB)
                                    val baseMapImageGraphics = baseMapImage.createGraphics()
                                    baseMapImageGraphics.setRenderingHint(
                                        RenderingHints.KEY_INTERPOLATION,
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR
                                    )

                                    val mapBackground = ImageIO.read(DreamMapWatermarker::class.java.getResource("/loricoolcards_sticker_map_background.png"))

                                    baseMapImageGraphics.color = Color(sticker.rarity.color.rgb)
                                    baseMapImageGraphics.fillRect(0, 0, 128, 128)
                                    val originalComposite = baseMapImageGraphics.composite
                                    val alphaComposite = AlphaComposite.getInstance(
                                        AlphaComposite.SRC_OVER,
                                        0.2f
                                    )
                                    baseMapImageGraphics.composite = alphaComposite
                                    baseMapImageGraphics.drawImage(mapBackground, 0, 0, null)
                                    baseMapImageGraphics.composite = originalComposite

                                    baseMapImageGraphics.drawImage(mapImage, 20, 1, 88, 126, null)

                                    // Create map
                                    val map = onMainThread {
                                        val map = Bukkit.createMap(Bukkit.getWorlds().first { it.name == "world" })

                                        map.isLocked = true // Optimizes the map because the server doesn't attempt to get the world data when the player is holding the map in their hand
                                        val renderers: List<MapRenderer> = map.renderers

                                        for (r in renderers) {
                                            map.removeRenderer(r)
                                        }

                                        map.addRenderer(ImgRenderer(MapPalette.imageToBytes(baseMapImage)))

                                        logger.info { "Created map for sticker ${sticker.id} (${sticker.fancyCardId} - ${sticker.title}) from album ${album.id} (${album.eventName})! Map ID: ${map.id}" }
                                        map
                                    }

                                    logger.info { "Saving map ${sticker.id} of (${sticker.fancyCardId} - ${sticker.title}) from album ${album.id} (${album.eventName}) on the disk..." }

                                    // Save map
                                    withContext(Dispatchers.IO) {
                                        ImageIO.write(baseMapImage, "png", File(m.imageFolder, "${map.id}.png"))
                                    }

                                    logger.info { "Saving map ${sticker.id} (${sticker.fancyCardId} - ${sticker.title}) from album ${album.id} (${album.eventName}) reference on the database..." }
                                    transaction(Dispatchers.IO, Databases.databaseNetwork) {
                                        LoriCoolCardsGeneratedMaps.insert {
                                            it[LoriCoolCardsGeneratedMaps.album] = album.id
                                            it[LoriCoolCardsGeneratedMaps.sticker] = sticker.id
                                            it[LoriCoolCardsGeneratedMaps.map] = map.id
                                            it[LoriCoolCardsGeneratedMaps.generatedAt] = Instant.now()
                                        }
                                    }

                                } catch (e: Exception) {
                                    logger.warn(e) { "Something went wrong while trying to process sticker ${sticker.id} of (${sticker.fancyCardId} - ${sticker.title}) from album ${album.id} (${album.eventName})!" }
                                }
                            }
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while trying to get stickers response of album ${album.id} (${album.eventName})!" }
                        }

                        logger.info { "Finished processing LoriCoolCards' album ${album.id} (${album.eventName})!" }
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to get albums!" }
                }

                logger.info { "Processed all LoriCoolCards stickers and albums! Waiting 60s before checking again..." }
                delay(60_000)
            }
        }
    }
}