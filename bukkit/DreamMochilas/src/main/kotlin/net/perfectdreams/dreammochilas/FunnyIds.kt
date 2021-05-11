package net.perfectdreams.dreammochilas

object FunnyIds {
    var names = mutableListOf<String>()
    var adjectives = mutableListOf<String>()

    fun generatePseudoId(): String {
        return "${names.random().capitalize()} ${adjectives.random().capitalize()} ${adjectives.random().capitalize()}"
    }
}