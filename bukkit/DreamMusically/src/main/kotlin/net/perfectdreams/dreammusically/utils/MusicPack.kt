package net.perfectdreams.dreammusically.utils

data class MusicPack(
    val name: String,
    val damage: Int,
    val play: String
) {
    companion object {
        val musicPacks = mutableListOf(
            MusicPack(
                "Jazz Jackrabbit 3 - Shop",
                25,
                "perfectdreams.sfx.jj3_shop"
            ),
            MusicPack(
                "Sonic Heroes - Special Stage",
                26,
                "perfectdreams.sfx.special_stage"
            ),
            MusicPack(
                "AKIRA - Kaneda's Theme",
                27,
                "perfectdreams.record.kaneda"
            ),
            MusicPack(
                "Ed Sheeran - Shape Of You",
                28,
                "perfectdreams.record.shapeofyou"
            ),
            MusicPack(
                "bbno$ & y2k - lalala",
                29,
                "perfectdreams.record.lalala"
            ),
            MusicPack(
                "Cash Cash - Kiss The Sky",
                30,
                "perfectdreams.record.kissthesky"
            ),
            MusicPack(
                "Boney M - Rasputin",
                31,
                "perfectdreams.record.rasputin"
            ),
            MusicPack(
                "Noisestorm - Crab Rave",
                32,
                "perfectdreams.record.crabrave"
            ),
            MusicPack(
                "CapitanSparklez - Revenge",
                33,
                "perfectdreams.record.revenge"
            ),
            MusicPack(
                "The Sims 1 - Buy Mode 3",
                34,
                "perfectdreams.sfx.buymode3"
            ),
            MusicPack(
                "MC Chinelinho - Chamei os Parça",
                35,
                "perfectdreams.record.chameiosparca"
            ),
            MusicPack(
                "Justin Bieber - What Do You Mean",
                36,
                "perfectdreams.record.whatdoyoumean"
            ),
            MusicPack(
                "Major Lazer - Lean On",
                37,
                "perfectdreams.record.leanon"
            ),
            MusicPack(
                "Hampton and the Hampsters - Sing a Simple Song",
                38,
                "perfectdreams.record.sing_a_simple_song"
            ),
            MusicPack(
                "DJ Azeitona - Para de falar que tu é minha namorada",
                39,
                "perfectdreams.record.dj_azeitona"
            )
        )
    }
}