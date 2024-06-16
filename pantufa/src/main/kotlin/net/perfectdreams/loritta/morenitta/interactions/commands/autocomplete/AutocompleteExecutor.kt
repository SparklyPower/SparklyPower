package net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete

fun interface AutocompleteExecutor<T> {
    suspend fun execute(
        context: AutocompleteContext,
    ): Map<String, T>
}