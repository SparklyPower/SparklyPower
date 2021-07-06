package net.perfectdreams.dreamchat.utils

fun main() {
    val wordTags = listOf(
        WordTagFitter("SPARKLYPOWER"),
        WordTagFitter("SPARKLY"),
        WordTagFitter("POWER"),
        WordTagFitter("LORITTA"),
        WordTagFitter("PANTUFA"),
        WordTagFitter("FELIZ"),
        WordTagFitter("DILMA"),
        WordTagFitter("CRAFT"),
        WordTagFitter("LORI"),
        WordTagFitter("MINE"),
        WordTagFitter("DIMA")
    )

    val allTags = listOf(
        PlayerTag(
            "D",
            "D"
        )
    )

    val tags = mutableListOf(
        PlayerTag(
            "C",
            "C"
        ),
        PlayerTag(
            "C",
            "C"
        ),
        PlayerTag(
            "F",
            "F"
        ),
        PlayerTag(
            "I",
            "I"
        ),
        PlayerTag(
            "M",
            "M"
        )
    )

    wordTags.forEach { wordTag ->
        tags.forEach { tag ->
            wordTag.tryFittingInto(tag)
        }
    }

    wordTags.forEach {
        println(it.text)
        println(it.tagPositions.map { it?.small })
    }

    val whatTagShouldBeUsed = wordTags.maxByOrNull {
        it.tagPositions.filterNotNull().distinct().size
    }

    println("Going to use tag ${whatTagShouldBeUsed?.text}")
}