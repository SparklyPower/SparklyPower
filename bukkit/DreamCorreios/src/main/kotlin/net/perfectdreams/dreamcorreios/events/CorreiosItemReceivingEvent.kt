package net.perfectdreams.dreamcorreios.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import java.util.*

class CorreiosItemReceivingEvent(
	val playerId: UUID,
	val player: Player?,
	val items: List<ItemStack>,
	var result: Result
) : Event() {
	override fun getHandlers(): HandlerList = Companion.handlers

	companion object {
		private val handlers = HandlerList()

		@JvmStatic
		fun getHandlerList(): HandlerList = handlers
	}

	sealed class Result(val message: Component)

	object PlayerIsAbleToReceiveItemsOnTheirInventoryResult : Result(
		textComponent {
			color(NamedTextColor.GREEN)
			append("Como o seu inventário está cheio, os itens foram enviados para a sua caixa postal! Acesse a sua caixa postal em ")
			appendCommand("/warp correios")
			append("!")
		}
	)

	object PlayerInEventResult : Result(
		textComponent {
			color(NamedTextColor.GREEN)
			append("Como você está em um evento, os itens foram enviados para a sua caixa postal! Acesse a sua caixa postal em ")
			appendCommand("/warp correios")
			append("!")
		}
	)

	class CustomResult(message: Component) : Result(message)
}