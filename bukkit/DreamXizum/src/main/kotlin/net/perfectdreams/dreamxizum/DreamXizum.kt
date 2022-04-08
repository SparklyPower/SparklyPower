package net.perfectdreams.dreamxizum

import kernitus.plugin.OldCombatMechanics.OCMMain
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamxizum.battle.BattleListener
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.commands.*
import net.perfectdreams.dreamxizum.commands.declarations.AcceptTOSCommand
import net.perfectdreams.dreamxizum.commands.declarations.DreamXizumCommand
import net.perfectdreams.dreamxizum.commands.declarations.X1Command
import net.perfectdreams.dreamxizum.commands.declarations.XizumCommand
import net.perfectdreams.dreamxizum.commands.subcommands.*
import net.perfectdreams.dreamxizum.config.XizumConfig
import net.perfectdreams.dreamxizum.extensions.unavailablePlayers
import net.perfectdreams.dreamxizum.lobby.CitizensNPCs
import net.perfectdreams.dreamxizum.lobby.Holograms
import net.perfectdreams.dreamxizum.lobby.Lobby
import net.perfectdreams.dreamxizum.tables.Combatants
import net.perfectdreams.dreamxizum.tables.Duelists
import net.perfectdreams.dreamxizum.tables.Kits
import net.perfectdreams.dreamxizum.tasks.RankedQueueTask
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DreamXizum : KotlinPlugin() {
    companion object {
        var legacyAttackSpeed = 24.0
        lateinit var INSTANCE: DreamXizum
        lateinit var luckPerms: LuckPerms
        const val COLOR = "§x§9§c§e§a§6§4"
        const val PREFIX = "§8[§x§2§e§9§0§d§1§lX1§8]$COLOR"
        fun highlight(text: String) = "§x§2§2§d§d§4§9$text$COLOR"
    }

    override fun softEnable() {
        super.softEnable()

        INSTANCE = this
        luckPerms = LuckPermsProvider.get()

        transaction(Databases.databaseNetwork) {
            SchemaUtils.createMissingTablesAndColumns(
                Combatants, Duelists, Kits
            )
        }

        loadConfig()
        registerCommands()
        registerEvents(Lobby())
        registerEvents(BattleListener())
        Holograms.spawnHolograms()
        Holograms.createLeaderboard()
        CitizensNPCs.loadFile(File(dataFolder, "citizens.json"))

        RankedQueueTask.startTask()
    }

    override fun softDisable() {
        with (Matchmaker) { battles.forEach { cancelBattle(it) } }
        unavailablePlayers.forEach { it.teleportToServerSpawn() }
    }

    fun loadConfig() {
        dataFolder.mkdir()
        File(dataFolder, "configuration.json").apply {
            if (!exists()) saveResource(name, true)
            XizumConfig.loadFile(this)
            legacyAttackSpeed = getPlugin(OCMMain::class.java).config.getDouble("disable-attack-cooldown.generic-attack-speed")
        }
    }

    private fun registerCommands() {
        registerCommand(AcceptTOSCommand, AcceptTOSExecutor(this))

        arrayOf(
            DreamXizumReloadExecutor(this), DreamXizumCancelExecutor(), DreamXizumBanExecutor(this)
        ).apply { registerCommand(DreamXizumCommand, *this) }

        arrayOf(
            XizumExecutor(this), XizumAcceptExecutor(this), XizumRefuseExecutor(this),
            XizumInviteExecutor(this), XizumRemoveExecutor(this), XizumRankExecutor(this)
        ).apply {
            registerCommand(XizumCommand, *this)
            registerCommand(X1Command, *this)
        }
    }
}