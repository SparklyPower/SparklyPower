package net.perfectdreams.dreammusically.utils

data class Song(
    val name: String,
    val soundName: String,
    val duration: Long // em segundos
) {
    companion object {
        val songs = mutableListOf(
            Song(
                "You Make Me",
                "music_disc.blocks",
                233
            ),
            Song(
                "White Walls",
                "music_disc.cat",
                220
            ),
            Song(
                "Don't You Worry Child",
                "music_disc.13",
                212
            ),
            Song(
                "Atomic",
                "music_disc.stal",
                204
            ),
            Song(
                "What Do You Mean",
                "music_disc.ward",
                207
            ),
            Song(
                "Spotlight",
                "music_disc.mellohi",
                187
            ),
            Song(
                "The Drum",
                "music_disc.chirp",
                129
            ),
            Song(
                "Funk do Yudi",
                "music_disc.11",
                79
            )
        )
    }
}