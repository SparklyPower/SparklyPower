package net.perfectdreams.dreamxizum.battle.elo

object Leagues {
    private val leagues = mapOf(
        2500 to Divisions.KNIGHTS,
        1600 to Divisions.GUARDIANS,
        1000 to Divisions.SQUIRES,
        400 to Divisions.DEFENDERS
    )

    fun getDivision(points: Int): Divisions {
        leagues.forEach { if (points >= it.key) return it.value }
        return Divisions.UNRANKED
    }
}

enum class Divisions(val league: String) {
    UNRANKED("Sem liga"),
    DEFENDERS("Defensores do Pudim"),
    SQUIRES("Escudeiros da Loritta"),
    GUARDIANS("Guardiões da Gabriela"),
    KNIGHTS("Cavaleiros da Pantufa")
}