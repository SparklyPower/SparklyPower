package net.perfectdreams.dreammapwatermarker.commands

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.dao.DiscordAccount
import net.perfectdreams.dreamcore.tables.DiscordAccounts
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.InventoryUtils
import net.perfectdreams.dreamcore.utils.adventure.*
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcorreios.DreamCorreios
import net.perfectdreams.dreammapwatermarker.DreamMapWatermarker
import net.perfectdreams.dreammapwatermarker.loricoolcards.CardRarity
import net.perfectdreams.dreammapwatermarker.loricoolcards.LoriCoolCardsFinishedAlbum
import net.perfectdreams.dreammapwatermarker.loricoolcards.LoriCoolCardsSticker
import net.perfectdreams.dreammapwatermarker.tables.LoriCoolCardsClaimedAlbums
import net.perfectdreams.dreammapwatermarker.tables.LoriCoolCardsGeneratedMaps
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.FunnyIds
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.utils.MochilaData
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import net.perfectdreams.exposedpowerutils.sql.transaction
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class LoriCoolCardsCommand(val m: DreamMapWatermarker) : SparklyCommandDeclarationWrapper {
    companion object {
        private val FIGURITTAS_LOGO_COLOR = TextColor.color(252, 179, 0)

        private fun prefix() = textComponent {
            append("[") {
                color(NamedTextColor.DARK_GRAY)
            }

            append("Figurittas") {
                color(FIGURITTAS_LOGO_COLOR)
                decorate(TextDecoration.BOLD)
            }

            append("]") {
                color(NamedTextColor.DARK_GRAY)
            }
        }
    }

    override fun declaration() = sparklyCommand(listOf("figurittas")) {
        executor = FigurittasExecutor(m, false)
    }

    class FigurittasExecutor(val m: DreamMapWatermarker, val opBypass: Boolean) : SparklyCommandExecutor() {
        // The "opBypass" variable bypasses any claim/album completion/etc checks
        override fun execute(context: CommandContext, args: CommandArguments) {
            val player = context.requirePlayer()

            m.launchAsyncThread {
                // Do we have a Discord account?
                val discordAccount = transaction(Dispatchers.IO, Databases.databaseNetwork) {
                    DiscordAccount.find { DiscordAccounts.minecraftId eq player.uniqueId and (DiscordAccounts.isConnected eq true) }.firstOrNull()
                }

                if (discordAccount == null) {
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        append(prefix())
                        appendSpace()
                        append("Você precisa conectar a sua conta do Discord com a sua conta do SparklyPower antes de poder resgatar as figurinhas das Figurittas da Loritta!")
                    }
                    return@launchAsyncThread
                }

                val albumsResponse = DreamUtils.http.get("${m.config.lorittaInternalApiUrl.removeSuffix("/")}/sparklypower/loricoolcards/users/${discordAccount.discordId}/albums")

                val finishedAlbums = Json.decodeFromString<List<LoriCoolCardsFinishedAlbum>>(albumsResponse.bodyAsText())

                if (finishedAlbums.isEmpty()) {
                    // You haven't finished any albums!
                    context.sendMessage {
                        color(NamedTextColor.RED)
                        append(prefix())
                        appendSpace()
                        append("Você não tem nenhum álbum completo das Figurittas da Loritta na Loritta! Compre figurinhas e complete seu álbum usando ")
                        appendCommand("/figurittas")
                        append(" na Loritta pelo Discord!")
                    }
                    return@launchAsyncThread
                }

                onMainThread {
                    // TODO: This WILL break after the user has 54 completed albums
                    val menu = createMenu(InventoryUtils.roundToNearestMultipleOfNine(finishedAlbums.size), "Escolha um Álbum") {
                        for ((index, finishedAlbum) in finishedAlbums.withIndex()) {
                            slot(index) {
                                item = ItemStack(Material.BOOK)
                                    .meta<ItemMeta> {
                                        displayNameWithoutDecorations {
                                            color(FIGURITTAS_LOGO_COLOR)
                                            decorate(TextDecoration.BOLD)
                                            content(finishedAlbum.album.eventName)
                                        }

                                        lore {
                                            textWithoutDecorations {
                                                // Define the desired format (e.g., "MMM dd, yyyy - HH:mm:ss z")
                                                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")

                                                // Format the ZonedDateTime
                                                val humanizedDateTime: String = Instant.ofEpochMilli(finishedAlbum.finishedAt).atZone(ZoneId.of("America/Sao_Paulo")).format(formatter)

                                                color(NamedTextColor.GRAY)

                                                append("Você completou este álbum em ")
                                                appendTextComponent {
                                                    color(NamedTextColor.YELLOW)
                                                    content(humanizedDateTime)
                                                }
                                                append(",")
                                            }

                                            textWithoutDecorations {
                                                color(NamedTextColor.GRAY)
                                                append("tornando-se a ")
                                                appendTextComponent {
                                                    color(NamedTextColor.GREEN)
                                                    content("${finishedAlbum.finishedPosition}ª pessoa")
                                                }
                                                append(" a terminá-lo!")
                                            }

                                            emptyLine()
                                            textWithoutDecorations {
                                                color(NamedTextColor.GREEN)
                                                decoration(TextDecoration.BOLD, true)
                                                content("Clique para resgatar as figurinhas deste álbum!")
                                            }
                                        }
                                    }

                                onClick {
                                    openSelectBackpackMenu(context, player, finishedAlbums, finishedAlbum)
                                }
                            }
                        }
                    }

                    menu.sendTo(player)
                }
            }
        }

        fun openSelectBackpackMenu(context: CommandContext, player: Player, finishedAlbums: List<LoriCoolCardsFinishedAlbum>, finishedAlbum: LoriCoolCardsFinishedAlbum) {
            // Open a NEW menu asking what backpack the user wants to redeem
            val backpacks = listOf(
                MochilaDataWithRarity(MochilaData.StickerRarityCommon, CardRarity.COMMON),
                MochilaDataWithRarity(MochilaData.StickerRarityUncommon, CardRarity.UNCOMMON),
                MochilaDataWithRarity(MochilaData.StickerRarityRare, CardRarity.RARE),
                MochilaDataWithRarity(MochilaData.StickerRarityEpic, CardRarity.EPIC),
                MochilaDataWithRarity(MochilaData.StickerRarityLegendary, CardRarity.LEGENDARY),
                MochilaDataWithRarity(MochilaData.StickerRaritySpecial, CardRarity.MYTHIC)
            )

            val menu = createMenu(9, "Escolha uma Mochila") {
                for ((index, mochilaDataWithRarity) in backpacks.withIndex()) {
                    slot(index) {
                        // Zero indexed, that's why we use > not >=
                        if (opBypass || finishedAlbums.size > index) {
                            item = ItemStack(Material.PAPER)
                                .meta<ItemMeta> {
                                    displayNameWithoutDecorations {
                                        color(TextColor.color(mochilaDataWithRarity.rarity.color.rgb))
                                        decorate(TextDecoration.BOLD)
                                        content(mochilaDataWithRarity.rarity.fancyName)
                                    }
                                    setCustomModelData(mochilaDataWithRarity.mochilaData.customModelData)
                                }

                            onClick {
                                it.closeInventory()

                                m.launchAsyncThread {
                                    // *Technically* we don't need to recheck if the user has the album completed, because if it is already completed it probably SHOULDN'T be removed from the album list
                                    val stickersOfThatAlbumResponse = DreamUtils.http.get("${m.config.lorittaInternalApiUrl.removeSuffix("/")}/sparklypower/loricoolcards/albums/${finishedAlbum.album.id}/stickers")

                                    val stickers = Json.decodeFromString<List<LoriCoolCardsSticker>>(stickersOfThatAlbumResponse.bodyAsText())
                                        .sortedBy { it.fancyCardId }

                                    val mapsData = transaction(Dispatchers.IO, Databases.databaseNetwork) {
                                        LoriCoolCardsGeneratedMaps.selectAll()
                                            .where { LoriCoolCardsGeneratedMaps.sticker inList stickers.map { it.id } }
                                            .toList()
                                    }

                                    val generatedMapIds = mapsData.map { it[LoriCoolCardsGeneratedMaps.sticker] }.toSet()
                                    if (generatedMapIds.size != stickers.size) {
                                        // Not fully generated yet!
                                        onMainThread {
                                            context.sendMessage {
                                                color(NamedTextColor.RED)
                                                append(prefix())
                                                appendSpace()
                                                append("As figurinhas deste álbum ainda não foram 100% geradas! Tente novamente mais tarde.")
                                            }
                                        }
                                        return@launchAsyncThread
                                    }

                                    // TECHNICALLY we don't need to use a Mutex because in this transaction we already check + add the user to the claimed albums, so it technically should error out if the user is in a "claimed" state
                                    if (!opBypass) {
                                        val hasAlreadyClaimedTheAlbum =
                                            transaction(Dispatchers.IO, Databases.databaseNetwork) {
                                                val hasAlreadyClaimedTheAlbum = LoriCoolCardsClaimedAlbums.selectAll()
                                                    .where { LoriCoolCardsClaimedAlbums.finishedId eq finishedAlbum.album.id }
                                                    .count() == 1L

                                                if (hasAlreadyClaimedTheAlbum)
                                                    return@transaction true

                                                LoriCoolCardsClaimedAlbums.insert {
                                                    it[claimedBy] = player.uniqueId
                                                    it[albumId] = finishedAlbum.album.id
                                                    it[finishedId] = finishedAlbum.id
                                                    it[claimedAt] = Instant.now()
                                                }
                                                return@transaction false
                                            }

                                        if (hasAlreadyClaimedTheAlbum) {
                                            onMainThread {
                                                context.sendMessage {
                                                    color(NamedTextColor.RED)
                                                    append(prefix())
                                                    appendSpace()
                                                    append("Você já reinvindicou as figurinhas deste álbum! Você só pode reinvindicar as figurinhas do álbum uma única vez.")
                                                }
                                            }
                                            return@launchAsyncThread
                                        }
                                    }

                                    val stickersAsItemStacks = mutableListOf<ItemStack>()

                                    for (sticker in stickers) {
                                        val mapDataOfSticker = mapsData.first { it[LoriCoolCardsGeneratedMaps.sticker].toLong() == sticker.id }

                                        stickersAsItemStacks.add(
                                            ItemStack(Material.FILLED_MAP)
                                                .meta<MapMeta> {
                                                    this.displayNameWithoutDecorations {
                                                        color(TextColor.color(sticker.rarity.color.rgb))
                                                        append(sticker.fancyCardId)
                                                        append(
                                                            textComponent {
                                                                color(NamedTextColor.DARK_GRAY)
                                                                append(" - ")
                                                            }
                                                        )
                                                        append(sticker.title)
                                                    }

                                                    this.lore {
                                                        textWithoutDecorations {
                                                            color(NamedTextColor.GRAY)
                                                            append("Figurinha do Álbum \"")
                                                            append(FIGURITTAS_LOGO_COLOR, finishedAlbum.album.eventName)
                                                            append("\"")
                                                        }

                                                        emptyLine()

                                                        textWithoutDecorations {
                                                            append(NamedTextColor.AQUA, "Raridade: ")
                                                            append(NamedTextColor.WHITE, sticker.rarity.emoji)
                                                            appendSpace()
                                                            appendTextComponent {
                                                                color(TextColor.color(sticker.rarity.color.rgb))
                                                                decoration(TextDecoration.BOLD, true)
                                                                content(sticker.rarity.fancyName.uppercase())
                                                            }
                                                        }
                                                    }

                                                    setCustomModelData(sticker.rarity.itemCustomModelData)

                                                    this.mapId = mapDataOfSticker[LoriCoolCardsGeneratedMaps.map]
                                                }.also { DreamMapWatermarker.watermarkMap(it, null) }
                                        )
                                    }

                                    val mochilasItemStacks = mutableListOf<ItemStack>()

                                    for (stickersAsItemStacksChunked in stickersAsItemStacks.chunked(54)) {
                                        // We will manually create a mochila here
                                        val newInventory = Bukkit.createInventory(null, 54, "Mochila")
                                        val funnyId = FunnyIds.generatePseudoId()

                                        newInventory.addItem(*stickersAsItemStacksChunked.toTypedArray())

                                        val mochila = transaction(Dispatchers.IO, Databases.databaseNetwork) {
                                            Mochila.new {
                                                this.owner = player.uniqueId
                                                this.size = 54
                                                this.content = MochilaUtils.serializeMochilaInventory(newInventory)
                                                this.type = mochilaDataWithRarity.mochilaData.customModelData
                                                this.funnyId = funnyId
                                                this.version = 1
                                            }
                                        }

                                        mochilasItemStacks.add(
                                            DreamMochilas.createMochila(mochilaDataWithRarity.mochilaData).meta<ItemMeta> {
                                                lore {
                                                    textWithoutDecorations {
                                                        color(NamedTextColor.GRAY)
                                                        content("Mochila de ")
                                                        appendTextComponent {
                                                            color(NamedTextColor.AQUA)
                                                            content(player.name)
                                                        }
                                                    }

                                                    emptyLine()

                                                    textWithoutDecorations {
                                                        color(NamedTextColor.GOLD)
                                                        content(funnyId)
                                                    }

                                                    emptyLine()

                                                    textWithoutDecorations {
                                                        color(NamedTextColor.GRAY)
                                                        appendTextComponent {
                                                            color(NamedTextColor.AQUA)
                                                            content(player.name)
                                                        }

                                                        append(" completou o álbum \"")
                                                        appendTextComponent {
                                                            color(FIGURITTAS_LOGO_COLOR)
                                                            content(finishedAlbum.album.eventName)
                                                        }
                                                        append("\" da Loritta")
                                                    }

                                                    textWithoutDecorations {
                                                        color(NamedTextColor.GRAY)

                                                        // Define the desired format (e.g., "MMM dd, yyyy - HH:mm:ss z")
                                                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")

                                                        // Format the ZonedDateTime
                                                        val humanizedDateTime: String =
                                                            Instant.ofEpochMilli(finishedAlbum.finishedAt)
                                                                .atZone(ZoneId.of("America/Sao_Paulo"))
                                                                .format(formatter)

                                                        append("em ")
                                                        appendTextComponent {
                                                            color(NamedTextColor.YELLOW)
                                                            content(humanizedDateTime)
                                                        }
                                                        append(", tornando-se a ")
                                                        appendTextComponent {
                                                            color(NamedTextColor.GREEN)
                                                            content("${finishedAlbum.finishedPosition}ª pessoa")
                                                        }
                                                        append(" a terminá-lo!")
                                                    }

                                                    emptyLine()

                                                    textWithoutDecorations {
                                                        append(NamedTextColor.AQUA, "Raridade: ")
                                                        append(NamedTextColor.WHITE, mochilaDataWithRarity.rarity.emoji)
                                                        appendSpace()
                                                        appendTextComponent {
                                                            color(TextColor.color(mochilaDataWithRarity.rarity.color.rgb))
                                                            decoration(TextDecoration.BOLD, true)
                                                            content(mochilaDataWithRarity.rarity.fancyName.uppercase())
                                                        }
                                                    }
                                                }

                                                persistentDataContainer.set(
                                                    MochilaUtils.MOCHILA_ID_KEY,
                                                    PersistentDataType.LONG,
                                                    mochila.id.value
                                                )
                                            }
                                        )
                                    }

                                    onMainThread {
                                        // Give out via correios
                                        DreamCorreios.getInstance().addItem(
                                            player,
                                            *mochilasItemStacks.toTypedArray()
                                        )

                                        context.sendMessage {
                                            color(NamedTextColor.GREEN)
                                            append(prefix())
                                            appendSpace()
                                            append("Você resgatou as ${stickers.size} figurinhas do álbum \"")
                                            appendTextComponent {
                                                color(FIGURITTAS_LOGO_COLOR)
                                                content(finishedAlbum.album.eventName)
                                            }
                                            append("\", divirta-se!")
                                        }
                                    }
                                }
                            }
                        } else {
                            item = ItemStack(Material.BARRIER)
                                .meta<ItemMeta> {
                                    displayNameWithoutDecorations {
                                        color(NamedTextColor.DARK_RED)
                                        decorate(TextDecoration.BOLD)
                                        content("BLOQUEADO")
                                    }

                                    lore {
                                        textWithoutDecorations {
                                            color(NamedTextColor.GRAY)
                                            content("Desbloqueado ao completar ${index + 1} álbuns")
                                        }
                                    }
                                }

                            onClick {
                                it.closeInventory()

                                context.sendMessage {
                                    color(NamedTextColor.RED)
                                    append(prefix())
                                    appendSpace()
                                    append("Esta mochila está bloqueada! Complete mais álbuns de figurinhas das Figurittas da Loritta para desbloquear mais tipos de mochilas!")
                                }
                            }
                        }
                    }
                }
            }

            menu.sendTo(player)
        }
    }

    class MochilaDataWithRarity(
        val mochilaData: MochilaData,
        val rarity: CardRarity
    )
}