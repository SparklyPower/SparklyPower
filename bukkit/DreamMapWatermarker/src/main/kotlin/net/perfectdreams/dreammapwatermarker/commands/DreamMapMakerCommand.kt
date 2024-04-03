package net.perfectdreams.dreammapwatermarker.commands

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammapwatermarker.DreamMapWatermarker
import net.perfectdreams.dreammapwatermarker.map.ImgRenderer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class DreamMapMakerCommand(val m: DreamMapWatermarker) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreammapmaker")) {
        subcommand(listOf("imagemap")) {
            permissions = listOf("dreammapmaker.imagemap")
            executor = ImageMapExecutor()
        }

        subcommand(listOf("dumprenderers")) {
            permissions = listOf("dreammapmaker.dumprenderers")
            executor = DumpRenderersExecutor()
        }
    }

    inner class ImageMapExecutor : SparklyCommandExecutor() {
        inner class Options : CommandOptions() {
            val urlOrFileName = greedyString("url_or_file_name")
        }

        override val options = Options()

        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val urlOrFileName = args[options.urlOrFileName]
            m.launchAsyncThread {
                context.sendMessage {
                    color(NamedTextColor.YELLOW)
                    content("Tentando baixar a imagem...")
                }

                val fileData = DreamUtils.http.get(urlOrFileName) {}
                val imageData = try {
                    ImageIO.read(fileData.readBytes().inputStream())
                } catch (e: IOException) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        content("Imagem não pode ser baixada!")
                    }
                    return@launchAsyncThread
                }

                val resizedImageData = if (imageData.width != 128 || imageData.height != 128) {
                    context.sendMessage {
                        color(NamedTextColor.YELLOW)
                        content("A imagem não é 128x128! Redimensionando a imagem...")
                    }

                    m.toBufferedImage(imageData.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH))
                } else {
                    imageData
                }

                onMainThread {
                    val heldItem = player.inventory.itemInMainHand

                    var newMap = false

                    val map = if (heldItem.type == Material.FILLED_MAP) {
                        context.sendMessage {
                            color(NamedTextColor.YELLOW)
                            content("Como você está segurando um mapa na mão, irei usar o ID do mapa dele...")
                        }

                        val mapView = (heldItem.itemMeta as MapMeta).mapView
                        if (mapView == null) {
                            context.sendMessage {
                                color(NamedTextColor.RED)
                                content("Você está segurando um mapa vazio, é impossível pegar o ID deste mapa!")
                            }

                            return@onMainThread
                        }

                        mapView
                    } else {
                        context.sendMessage {
                            color(NamedTextColor.YELLOW)
                            content("Criando um novo mapa do zero...")
                        }

                        newMap = true

                        Bukkit.createMap(player.world)
                    }

                    context.sendMessage {
                        color(NamedTextColor.YELLOW)
                        content("Aplicando imagem no mapa...")
                    }

                    map.isLocked = true // Optimizes the map because the server doesn't attempt to get the world data when the player is holding the map in their hand
                    val renderers: List<MapRenderer> = map.renderers

                    for (r in renderers) {
                        map.removeRenderer(r)
                    }

                    map.addRenderer(ImgRenderer(MapPalette.imageToBytes(resizedImageData)))

                    if (newMap) {
                        context.sendMessage {
                            color(NamedTextColor.GREEN)
                            content("Mapa criado com sucesso! ID: ${map.id}")
                        }

                        // Give the player the map if it was a map created from scratch
                        player.inventory.addItem(
                            ItemStack(Material.FILLED_MAP)
                                .meta<MapMeta> {
                                    this.mapId = map.id
                                }
                        )

                        context.sendMessage {
                            content("Como eu tirei o novo mapa do meu fiofo, eu adicionei o mapa no seu inventário!")
                            color(NamedTextColor.YELLOW)
                        }

                        context.sendMessage {
                            color(NamedTextColor.YELLOW)
                            append("Se precisar, você pode se dar o mapa usando ")
                            appendCommand("/give ${player.name} minecraft:filled_map{map:${map.id}}")
                        }
                    } else {
                        context.sendMessage {
                            color(NamedTextColor.GREEN)
                            content("Mapa alterado com sucesso! ID: ${map.id}")
                        }

                        context.sendMessage {
                            color(NamedTextColor.YELLOW)
                            append("Se precisar, você pode se dar o mapa usando ")
                            appendCommand("/give ${player.name} minecraft:filled_map{map:${map.id}}")
                        }
                    }

                    context.sendMessage {
                        color(NamedTextColor.YELLOW)
                        content("Como a imagem foi aplicada no mapa corretamente, irei salvar a imagem para que ela seja restaurada no mapa quando o servidor reiniciar...")
                    }

                    onAsyncThread {
                        withContext(Dispatchers.IO) {
                            ImageIO.write(resizedImageData, "png", File(m.imageFolder, "${map.id}.png"))
                        }
                    }

                    context.sendMessage {
                        color(NamedTextColor.GREEN)
                        content("Imagem salva com sucesso!")
                    }
                }
            }
        }
    }

    inner class DumpRenderersExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            val map = player.inventory.itemInMainHand
            if (map.type != Material.FILLED_MAP) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Você precisa estar segurando um mapa na sua mão!")
                }
                return
            }

            val mapMeta = map.itemMeta as MapMeta

            val mapView = mapMeta.mapView

            if (mapView == null) {
                context.sendMessage {
                    color(NamedTextColor.RED)
                    content("Mapa não tem um Map View!")
                }
                return
            }

            context.sendMessage {
                color(NamedTextColor.AQUA)
                content("Renderers do Mapa:")
            }
            mapView.renderers.forEach {
                context.sendMessage {
                    color(NamedTextColor.YELLOW)
                    content("$it")
                }
            }
        }
    }
}