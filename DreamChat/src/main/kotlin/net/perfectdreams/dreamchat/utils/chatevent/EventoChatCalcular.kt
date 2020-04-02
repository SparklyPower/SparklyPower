package net.perfectdreams.dreamchat.utils.chatevent

import net.perfectdreams.dreamcore.utils.DreamUtils
import org.bukkit.entity.Player

class EventoChatCalcular : IEventoChat {
	var calculation: Calculation? = null

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
		val calculation = calculation!!

		val str = buildString {
			this.append(calculation.first.toString())
			this.append(" ")
			if (calculation.type == Calculation.Type.PLUS)
				this.append("+")
			if (calculation.type == Calculation.Type.MINUS)
				this.append("-")
			if (calculation.type == Calculation.Type.MULTIPLICATION)
				this.append("*")
			this.append(" ")
			this.append(calculation.second.toString())
		}

		return str
	}

	override fun getToDoWhat(): String {
		return "calcular"
	}

	@Synchronized
	override fun process(player: Player, message: String): Boolean {
		if (calculation == null)
			return false

		return message.equals(calculation!!.getAnswer().toString(), true)
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