package net.perfectdreams.dreamlobbyfun.listeners

import com.xxmicloxx.NoteBlockAPI.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.SongEndEvent
import com.xxmicloxx.NoteBlockAPI.SoundCategory
import net.perfectdreams.dreamcore.utils.getRandom
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SongListener(val m: DreamLobbyFun) : Listener {
	@EventHandler
	fun onEnd(e: SongEndEvent) {
		m.songPlayer?.destroy()
		val newSongPlayer = RadioSongPlayer(m.songs.getRandom(), SoundCategory.RECORDS)
		m.logger.info("Tocando nova música! ${newSongPlayer.song.path}")
		newSongPlayer.autoDestroy = true
		Bukkit.getOnlinePlayers().forEach {
			newSongPlayer.addPlayer(it)
		}
		newSongPlayer.isPlaying = true
		m.songPlayer = newSongPlayer
	}
}