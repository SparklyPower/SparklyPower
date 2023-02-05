package net.perfectdreams.dreamsocial.gui.announcement

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.TextUtils.convertToNumeroNomeAdjetivo
import net.perfectdreams.dreamcore.utils.createMenu
import net.perfectdreams.dreamcore.utils.extensions.asComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.stripColorCode
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreamsocial.DreamSocial
import net.perfectdreams.dreamsocial.commands.announce.SaveAnnouncementExecutor
import net.perfectdreams.dreamsocial.dao.AnnouncementsEntity
import net.perfectdreams.dreamsocial.gui.confirmation.renderConfirmationMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.regex.Pattern

fun renderAnnouncementsMenu(plugin: DreamSocial, player: Player, announcementsEntity: AnnouncementsEntity, contentToSave: String? = null) =
    createMenu(9, "ꈉ§f\uE266§r陇§fSelecione um slot") {
        val slots = with (announcementsEntity) { listOf(firstSlot, secondSlot, thirdSlot) }

        slots.forEachIndexed { index, content ->
            slot(index + 3, 0) {
                item = getFloppyDisc(index, content)

                onClick {
                    if (contentToSave != null)
                        renderConfirmationMenu {
                            plugin.schedule(SynchronizationContext.ASYNC) {
                                transaction(Databases.databaseNetwork) {
                                    when(index) {
                                        0 -> announcementsEntity.firstSlot = contentToSave
                                        1 -> announcementsEntity.secondSlot = contentToSave
                                        2 -> announcementsEntity.thirdSlot = contentToSave
                                    }
                                }

                                switchContext(SynchronizationContext.SYNC)

                                it.closeInventory()
                                it.sendMessage("§aSeu anúncio foi salvo com sucesso!")
                                SaveAnnouncementExecutor.lastAnnouncements.remove(player)
                            }
                        }.sendTo(player)
                    else
                        content?.let {
                            player.closeInventory()
                            player.performCommand("anunciar $it")
                        }
                }
            }
        }
    }

/**
 * Generates a [floppy disc][ItemStack] that has its index + 1 as the number of items for visual representation
 * It is gray if the slot is unused, and is colored otherwise.
 */
@Suppress("DEPRECATION")
private fun getFloppyDisc(index: Int, content: String?) =
    ItemStack(Material.PAPER, index + 1)
        .meta<ItemMeta> {
            val ordinalNumber = (index + 1).convertToNumeroNomeAdjetivo()

            displayName(
                "$ordinalNumber Slot".asComponent
                    .decorations(mapOf(
                        TextDecoration.BOLD to TextDecoration.State.TRUE,
                        TextDecoration.ITALIC to TextDecoration.State.FALSE
                    ))
                    .color(TextColor.color(content?.let { 0x1C79E3 } ?: 0x889897))
            )

            lore = mutableListOf<String>().apply {
                if (content != null) {
                    val dividedLines = linesOfFixedLength(content, 45, 3).mapTo(mutableListOf()) { it.translateColorCodes() }

                    add("§7Prévia: ${dividedLines.removeFirst()}")
                    dividedLines.forEach(::add)
                }
                else add("§7Este slot está §adisponível §7para uso!")
            }

            setCustomModelData(content?.let { 80 } ?: 79)
        }

private val colorsPattern = Pattern.compile("((&[0-9A-FK-ORX])+)", Pattern.CASE_INSENSITIVE)

/**
 * Divides [message] into, at most, [maxLines] lines of [maxLength] each. Truncating it with "..." if need be.
 */
fun linesOfFixedLength(message: String, maxLength: Int, maxLines: Int): List<String> {
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()
    var startOfTheLine = true
    var currentLineLength = 0
    var currentLineIndex = 1
    var lastColor = "&7"

    val words = message.split(" ")

    for ((index, word) in words.withIndex()) {
        val spacedWord = if (startOfTheLine) word else " $word"

        startOfTheLine = false
        currentLine.append(spacedWord)
        currentLineLength += spacedWord.stripColorCode().length

        val hasAddedAllWords = (index + 1) == words.size

        if (currentLineIndex == maxLines) {
            if (currentLineLength >= maxLength || hasAddedAllWords) {
                if (!hasAddedAllWords) currentLine.append("§7...")
                lines.add(currentLine.toString())
                break
            }
        } else if (currentLineLength >= maxLength || hasAddedAllWords) {
            val builtString = currentLine.toString()
            val matcher = colorsPattern.matcher(builtString)
            var lastColorInLine: String? = null

            while (matcher.find()) lastColorInLine = matcher.group()

            lastColor = lastColorInLine ?: lastColor
            currentLine = StringBuilder(lastColor)
            startOfTheLine = true

            lines.add(builtString)
            currentLineLength = 0
            currentLineIndex++
        }
    }

    return lines.map(String::trimStart)
}