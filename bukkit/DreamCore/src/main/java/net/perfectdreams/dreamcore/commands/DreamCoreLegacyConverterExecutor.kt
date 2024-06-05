package net.perfectdreams.dreamcore.commands

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.adventure.hoverText
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions

class DreamCoreLegacyConverterExecutor(val m: DreamCore) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val text = greedyString("text")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val componentFromLegacy = LegacyComponentSerializer.legacyAmpersand().deserialize(args[options.text])
        val componentToMiniMessage = MiniMessage.miniMessage().serialize(componentFromLegacy)
        context.sendMessage {
            content(componentToMiniMessage)

            hoverText {
                content("Clique para copiar!")
            }

            clickEvent(ClickEvent.copyToClipboard(componentToMiniMessage))
        }
    }
}