package net.perfectdreams.dreamcore.utils.commands.options

import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import org.bukkit.entity.Player

typealias SuggestsBlock = ((CommandContext, SuggestionsBuilder) -> (Unit))?
sealed class CommandOption<T>(val name: String, val optional: Boolean, val suggestsBlock: SuggestsBlock)
class WordCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String>(name, false, suggestsBlock)
class QuotableStringCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String>(name, false, suggestsBlock)
class GreedyStringCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String>(name, false, suggestsBlock)
class OptionalGreedyStringCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<String?>(name, true, suggestsBlock)
class BooleanCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Boolean>(name, false, suggestsBlock)
class DoubleCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Double>(name, false, suggestsBlock)
class DoubleMinCommandOption(name: String, val min: Double, suggestsBlock: SuggestsBlock) : CommandOption<Double>(name, false, suggestsBlock)
class DoubleMinMaxCommandOption(name: String, val min: Double, val max: Double, suggestsBlock: SuggestsBlock) : CommandOption<Double>(name, false, suggestsBlock)
class PlayerCommandOption(name: String, suggestsBlock: SuggestsBlock) : CommandOption<Player>(name, false, suggestsBlock)