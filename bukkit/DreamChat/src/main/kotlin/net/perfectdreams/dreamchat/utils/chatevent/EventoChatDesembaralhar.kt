package net.perfectdreams.dreamchat.utils.chatevent

import org.bukkit.entity.Player

class EventoChatDesembaralhar : IEventoChat {
	val words = listOf(
		"SHULKER",
		"ABÓBORA",
		"CENOURA",
		"BATATA",
		"BETERRABA",
		"ESPADA",
		"LASANHA",
		"FEIJÃO",
		"ODOR",
		"ENXADA",
		"MOUSE",
		"TECLADO",
		"NOTEBOOK",
		"COMPUTADOR",
		"JOGO",
		"LORITTA",
		"PANTUFA",
		"GABRIELA",
		"MINECRAFT",
		"DIAMANTE",
		"ESMERALDA",
		"FERRO",
		"CARVÃO",
		"NETHERITE",
		"OURO",
		"EVENTO",
		"CHAT",
		"PORCO",
		"VACA",
		"GALINHA",
		"OVELHA",
		"LHAMA",
		"AXOLOTE",
		"RAPOSA",
		"BAIACU",
		"BURRO",
		"GATO",
		"GATA",
		"SORVETE",
		"PICOLÉ",
		"DOIDA",
		"MALUCA",
		"MOJANG",
		"YOUTUBE",
		"GOOGLE",
		"MICROSOFT",
		"SUPORTE",
		"ADMIN",
		"COORDENADOR",
		"COORDENADA",
		"DONO",
		"CAVALO",
		"JAGUATIRICA",
		"TARTARUGA",
		"SALMÃO",
		"PIGLIN",
		"MORCEGO",
		"ABELHA",
		"PANDA",
		"AFOGADO",
		"SAQUEADOR",
		"VEX",
		"DEVASTADOR",
		"PHANTOM",
		"WITHER",
		"ILUSIONISTA",
		"FLOR",
		"PICARETA",
		"MACHADO",
		"MADEIRA",
		"PEDRA",
		"AREIA",
		"TERRA",
		"GRAMA",
		"COOKIE",
		"TORTA",
		"MELÂNCIA",
		"BIFE",
		"ARMADURA",
		"ESTANTE",
		"ENCANTAMENTO",
		"EXPERIÊNCIA",
		"ROSA",
		"DOCE",
		"BAMBU",
		"FRUTA",
		"EVENTOS",
		"JETPACK",
		"MUNDO",
		"DINAMITE",
		"AMIGO",
		"AMOR",
		"BOLOR",
		"BRUTO",
		"CABER",
		"CEDER",
		"COISA",
		"CREDO",
		"CRIME",
		"CUSTO",
		"DIZER",
		"ERRO",
		"FALAR",
		"FAZER",
		"FEITO",
		"FICAR",
		"FINAL",
		"FUGIR",
		"GOSTO",
		"GRATO",
		"IRADO",
		"JOVEM",
		"JURAR",
		"LUTAR",
		"MALHA",
		"MIMAR",
		"MUITO",
		"NERVO",
		"NÍVEL",
		"ÓBVIO",
		"OUVIR",
		"PODER",
		"PROVA",
		"QUASE",
		"SABOR",
		"SOBRA",
		"TERNO",
		"TOTAL",
		"VALOR",
	)

	var currentWord: String? = null

	override fun preStart() {
		currentWord = words.random()
	}

	override fun getAnnouncementMessage(): String {
		val currentWord = currentWord!!

		var shuffledChars = currentWord.toCharArray().toList()

		while (shuffledChars.joinToString("") == currentWord)
			shuffledChars = shuffledChars.shuffled()

		val shuffledWord = shuffledChars.joinToString(separator = "")

		return shuffledWord
	}

	override fun getToDoWhat(): String {
		return "desembaralhar"
	}

	@Synchronized
	override fun process(player: Player, message: String): Boolean {
		if (currentWord == null)
			return false

		return message.equals(currentWord, true)
	}
}