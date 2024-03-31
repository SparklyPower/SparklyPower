package net.perfectdreams.dreamloja.commands

import net.kyori.adventure.text.TextComponent
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamloja.DreamLoja

abstract class LojaExecutorBase(val m: DreamLoja) : SparklyCommandExecutor() {
    fun CommandContext.sendLojaMessage(block: TextComponent.Builder.() -> (Unit) = {}) = sendMessage {
        append(DreamLoja.PREFIX)
        append(" ")

        append(
            textComponent {
                block.invoke(this)
            }
        )
    }
}