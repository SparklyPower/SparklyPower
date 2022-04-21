package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.WeatherType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.weather.WeatherChangeEvent

class WeatherListener(val m: DreamMini) : Listener {
    @EventHandler
    fun onWeather(e: WeatherChangeEvent) {
        val disabledRain = Bukkit.getOnlinePlayers().filter { m.weatherBlacklist.contains(it.uniqueId) && e.world == it.world }

        disabledRain.forEach {
            it.setPlayerWeather(WeatherType.CLEAR)

            if (e.toWeatherState()) {
                it.sendMessage("§eA chuva começou... Como você desativou a chuva, você não irá ver ela... Caso queria reativar a chuva, use §6/chuva§e!")
            } else {
                it.sendMessage("§eA chuva parou! Caso você está sentindo saudades de ver a chuva cair em vez de \"chover mas eu não consigo ver nada\", use §6/chuva§e!")
            }
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) { with (e.player) { if (uniqueId in m.weatherBlacklist) setPlayerWeather(WeatherType.CLEAR) } }
}