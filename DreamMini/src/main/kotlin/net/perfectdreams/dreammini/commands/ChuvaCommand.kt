package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.WeatherType
import org.bukkit.entity.Player

class ChuvaCommand(val m: DreamMini) : SparklyCommand(arrayOf("chuva")) {
    @Subcommand
    fun chuva(player: Player) {
        if (m.weatherBlacklist.contains(player.uniqueId)) {
            m.weatherBlacklist.remove(player.uniqueId)
            player.resetPlayerWeather()
            player.sendMessage("§aAgora você irá ver a chuva quando chover!")
        } else {
            m.weatherBlacklist.add(player.uniqueId)
            player.sendMessage("§aAgora você nunca mais irá ver chuva! Os efeitos da chuva ainda irão acontecer, mas você não irá *ver* ela! (Wow, mágica!)")

            player.setPlayerWeather(WeatherType.CLEAR)
        }
    }
}