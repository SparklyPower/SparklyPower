package net.perfectdreams.dreamlobbyfun.commands

import com.xxmicloxx.NoteBlockAPI.NBSDecoder
import com.xxmicloxx.NoteBlockAPI.RadioSongPlayer
import com.xxmicloxx.NoteBlockAPI.SoundCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import net.perfectdreams.dreamlobbyfun.utils.ServerCitizen
import net.perfectdreams.dreamlobbyfun.utils.ServerCitizenData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.File

class ConfigureServerCommand(val m: DreamLobbyFun) : AbstractCommand("configureserver", permission = "dreamlobby.configureserver") {
	@Subcommand(["create"])
	fun createServer(player: Player, citizenId: Int, serverName: String, fancyServerName: Array<String>) {
		val serverCitizenData = ServerCitizenData(citizenId.toInt(), serverName, fancyServerName.joinToString(" "))
		val serverCitizen = ServerCitizen(serverCitizenData, m)

		m.serverCitizens.add(serverCitizen)

		player.sendMessage("§aServidor §e${fancyServerName}§a (§e$serverName§a) foi configurado no NPC §e$citizenId§a com sucesso!")

		m.serverCitizensFile.writeText(Json.encodeToString(m.serverCitizens.map { it.data }))
	}

	@Subcommand(["delete"])
	fun deleteServer(player: Player, citizenId: Int) {
		val serverCitizen = m.serverCitizens.firstOrNull { it.data.citizenId == citizenId.toInt() }

		if (serverCitizen == null) {
			player.sendMessage("§cNPC §e${citizenId}§c não possui nenhum servidor associado!")
			return
		}

		serverCitizen.clickHereHologram?.delete()
		serverCitizen.serverNameHologram?.delete()
		serverCitizen.playerCountHologram?.delete()

		m.serverCitizens.remove(serverCitizen)
		player.sendMessage("§aServidor §e${serverCitizen.data.fancyServerName}§a (§e$serverCitizen.serverName§a) teve NPC §e$citizenId§a removido com sucesso!")

		m.serverCitizensFile.writeText(Json.encodeToString(m.serverCitizens.map { it.data }))
	}

	@Subcommand(["skip"])
	fun skipSong(player: Player) {
		// Ao remover todos os players do song player atual (e se auto destroy estiver como true), a música será parada, o que irá causar um
		// SongEndEvent ser ativado!
		m.songPlayer?.playerList?.mapNotNull { Bukkit.getPlayer(it) }?.forEach {
			m.songPlayer?.removePlayer(it)
		}
		player.sendMessage("§aTodos os players do Song Player atual foram removidos!")
	}

	@Subcommand(["playsong"])
	fun playSong(player: Player, songName: String) {
		m.songPlayer?.destroy()

		val songFile = File(m.songsFolder, songName)

		if (!songFile.exists()) {
			player.sendMessage("§cMúsica não existe!")
			return
		}

		val song = NBSDecoder.parse(songFile)
		val newSongPlayer = RadioSongPlayer(song, SoundCategory.RECORDS)
		m.logger.info("Forçando nova música! ${newSongPlayer.song.path}")
		newSongPlayer.autoDestroy = true
		Bukkit.getOnlinePlayers().forEach {
			newSongPlayer.addPlayer(it)
		}
		newSongPlayer.isPlaying = true
		m.songPlayer = newSongPlayer

		player.sendMessage("§aMúsica alterada!")
	}

	@Subcommand(["reloadsongs"])
	fun reloadSongs(player: Player) {
		m.loadSongs()
		player.sendMessage("§aMúsicas recarregadas com sucesso!")
	}
}