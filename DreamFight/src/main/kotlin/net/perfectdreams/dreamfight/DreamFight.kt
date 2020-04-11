package net.perfectdreams.dreamfight

import net.perfectdreams.dreamfight.commands.FightCommand
import net.perfectdreams.dreamfight.commands.FightSetupCommand
import net.perfectdreams.dreamfight.handlers.FightListener
import net.perfectdreams.dreamfight.utils.FightArena
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamfight.event.EventoFight
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class DreamFight : KotlinPlugin() {
    val fight = FightArena(this)
    val eventoFight = EventoFight(this)
    val dataYaml by lazy {
        File(dataFolder, "data.yml")
    }

    val userData by lazy {
        if (!dataYaml.exists())
            dataYaml.writeText("")

        YamlConfiguration.loadConfiguration(dataYaml)
    }

    override fun softEnable() {
        super.softEnable()
        dataFolder.mkdirs()

        // conf = Config(getDataFolder().toString() + "/fight.yml", Config.YAML)
        // fight = FightArena(this)
        // conf.set("version", 1)
        // conf.save()

        registerEvents(FightListener(this))

        if (userData.contains("Fight")) {
            fight.lobby = userData.getLocation("Fight.Lobby")!!
            fight.pos1 = userData.getLocation("Fight.Pos1")!!
            fight.pos2 = userData.getLocation("Fight.Pos2")!!
            fight.exit = userData.getLocation("Fight.Exit")!!

        }
        /* if (conf.exists("Fight")) {
            fight!!.lobby = SparklyUtils.deserializeLocation(conf.getString("Fight.Lobby"))
            fight!!.pos1 = SparklyUtils.deserializeLocation(conf.getString("Fight.Pos1"))
            fight!!.pos2 = SparklyUtils.deserializeLocation(conf.getString("Fight.Pos2"))
            fight!!.exit = SparklyUtils.deserializeLocation(conf.getString("Fight.Exit"))
            lastWinner = conf.getString("Fight.LastWinner")
        } */
        /* object : NukkitRunnable() {
            fun run() {
                if (getServer().getOnlinePlayers().size() >= 70) {
                    if (MettatonEX.canStartEvent() && !fight!!.started) {
                        var diff = System.currentTimeMillis() - lastFight
                        diff = TimeTools.convert(TimeUnit.MILLISECONDS, TimeUnit.MINUTES, diff)
                        if (diff >= 60) {
                            fight!!.preStartFight()
                        }
                    }
                }
            }
        }.runTaskTimer(this, 100L, 100L) */

        registerCommand(FightCommand)
        registerCommand(FightSetupCommand)
        registerServerEvent(eventoFight)
    }

    override fun softDisable() {
        super.softDisable()

        fight.shutdownFight()
        /* conf.set("Fight.Lobby", SparklyUtils.serializeLocation(fight!!.lobby))
        conf.set("Fight.Pos1", SparklyUtils.serializeLocation(fight!!.pos1))
        conf.set("Fight.Pos2", SparklyUtils.serializeLocation(fight!!.pos2))
        conf.set("Fight.Exit", SparklyUtils.serializeLocation(fight!!.exit))
        conf.set("Fight.LastWinner", lastWinner)
        conf.save() */
    }

    val me: DreamFight
        get() = this

    companion object {
        const val prefix = "§8[§4§lFight§8] "
        var lastWinner: String? = null
        var lastFight = 0L
    }
}