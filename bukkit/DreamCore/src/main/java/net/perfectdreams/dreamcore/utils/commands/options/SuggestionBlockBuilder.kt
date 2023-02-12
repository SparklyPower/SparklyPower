package net.perfectdreams.dreamcore.utils.commands.options

fun buildSuggestionsBlockFromList(options: () -> List<String>): SuggestsBlock = { _, builder ->
    options.invoke().forEach {
        if (it.startsWith(builder.remaining, true)) builder.suggest(it)
    }
}