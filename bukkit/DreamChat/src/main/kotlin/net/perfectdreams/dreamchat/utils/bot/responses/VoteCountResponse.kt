/* package net.perfectdreams.dreamchat.utils.bot.responses

import net.perfectdreams.dreamvote.DreamVote
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

class VoteCountResponse : RegExResponse() {
	init {
		patterns.add("quantos|quantas".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("voto|vezes".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("tenho|possuo|votei".toPattern(Pattern.CASE_INSENSITIVE))
	}

	override fun getResponse(message: String, event: AsyncPlayerChatEvent): String? {
		val vote = DreamVote.INSTANCE.getVoteCount(event.player)

		val response = when (vote) {
			in 35..39 -> "(Herobrine deveria ter medo de você por ter votado mais de 35 vezes)"
			in 30..34 -> "(eu te amo por ter votado mais de 30 vezes)"
			in 25..29 -> "(obrigado por ajudar o SparklyPower! mais de 25 votos não é fácil, mas você conseguiu!)"
			in 20..24 -> "(mais de 20 vezes??? se um dia eu fazer uma festa, vou te colocar na lista de pessoas importantes!)"
			in 15..19 -> "(kk eae men, já votou mais de 15 vezes? wow, você é daora!)"
			in 10..14 -> "(obrigado por votar mais de 10 vezes, você ajudou bastante o SparklyPower!)"
			in 5..9 -> "(wow, mais de 5 votos? você já está indo bem!)"
			in 1..4 -> "(poucos votos... mas eu sei que você pode fazer melhor!)"
			0 -> "(nenhum voto... :( se você quiser ajudar o servidor, por favor, vote! §6/votar§a)"
			else -> "(você é incrível.)"
		}
		return "§b" + event.player.displayName + "§a, você já votou §9${vote} vezes§a! §7${response}§a Você também pode ver seus votos no §6/votar§a! ;)"
	}
} */