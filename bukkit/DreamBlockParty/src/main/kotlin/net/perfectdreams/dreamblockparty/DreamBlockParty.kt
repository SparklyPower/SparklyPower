package net.perfectdreams.dreamblockparty

import com.xxmicloxx.NoteBlockAPI.model.Song
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamblockparty.commands.BlockPartyCommand
import net.perfectdreams.dreamblockparty.commands.DreamBlockPartyStartCommand
import net.perfectdreams.dreamblockparty.event.EventoBlockParty
import net.perfectdreams.dreamblockparty.listeners.PlayerListener
import net.perfectdreams.dreamblockparty.listeners.TagListener
import net.perfectdreams.dreamblockparty.utils.BlockParty
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import java.io.File

class DreamBlockParty : KotlinPlugin() {
    companion object {
        fun prefix() = textComponent {
            append("[") {
                color(NamedTextColor.DARK_GRAY)
            }

            append("Block Party") {
                color(NamedTextColor.AQUA)
                decorate(TextDecoration.BOLD)
            }

            append("]") {
                color(NamedTextColor.DARK_GRAY)
            }
        }
    }

    // These are lazy initialized because the BlockParty requires access to the MaterialColors object, but that's only loaded AFTER DreamCore loads
    lateinit var blockParty: BlockParty
    lateinit var eventoBlockParty: EventoBlockParty

    val songsFolder by lazy {
        dataFolder.mkdirs()
        val songs = File(dataFolder, "songs")
        songs.mkdirs()
        songs
    }
    val songs = mutableListOf<Song>()

    override fun softEnable() {
        super.softEnable()

        blockParty = BlockParty(this)
        eventoBlockParty = EventoBlockParty(this)

        this.dataFolder.mkdirs()

        registerEvents(PlayerListener(this))
        registerEvents(TagListener(this))
        registerServerEvent(eventoBlockParty)
        registerCommand(BlockPartyCommand(this))
        registerCommand(DreamBlockPartyStartCommand(this))

        loadSongs()
    }

    override fun softDisable() {
        super.softDisable()

        if (blockParty.isStarted) {
            if (blockParty.isPreStart) {
                blockParty.playersInQueue.toList().forEach { blockParty.removeFromQueue(it) }
            } else {
                blockParty.players.toList().forEach { blockParty.removeFromGame(it, skipFinishCheck = false) }
            }
        }
    }

    private fun loadSongs() {
        songs.clear()
        songs.addAll(
            songsFolder.listFiles().filter {
                it.extension == "nbs"
            }.map {
                NBSDecoder.parse(it)
            }
        )
    }
}