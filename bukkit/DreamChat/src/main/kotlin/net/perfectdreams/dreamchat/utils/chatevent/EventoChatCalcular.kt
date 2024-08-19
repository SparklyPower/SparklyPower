package net.perfectdreams.dreamchat.utils.chatevent

import net.perfectdreams.dreamcore.utils.DreamUtils
import org.bukkit.entity.Player

class EventoChatCalcular : IEventoChat {
	private var calculation: Calculation? = null
	var lastEventMessage: String? = null

	override fun preStart() {
		val randomNumber1 = DreamUtils.random.nextInt(0, 21)
		val randomNumber2 = DreamUtils.random.nextInt(0, 21)

		calculation = Calculation(
			randomNumber1,
			randomNumber2,
			Calculation.Type.values().random()
		)
	}

	override fun getAnnouncementMessage(): String {
		val calc = calculation ?: return ""

		return buildString {
			append(calc.first.toString())
			append(" ")
			when (calc.type) {
				Calculation.Type.PLUS -> append("+")
				Calculation.Type.MINUS -> append("-")
				Calculation.Type.MULTIPLICATION -> append("*")
			}
			append(" ")
			append(calc.second.toString())
		}
	}

	fun getCorrectAnswer(): String {
		return calculation?.getAnswer().toString() ?: ""
	}

	override fun getToDoWhat(): String {
		return "calcular"
	}

	@Synchronized
	override fun process(player: Player, message: String): Boolean {
		return message.equals(getCorrectAnswer(), true)
	}

	class Calculation(
		val first: Int,
		val second: Int,
		val type: Type
	) {
		enum class Type {
			PLUS,
			MINUS,
			MULTIPLICATION
		}

		fun getAnswer(): Int {
			return when (type) {
				Type.PLUS -> first + second
				Type.MINUS -> first - second
				Type.MULTIPLICATION -> first * second
			}
		}
	}
}