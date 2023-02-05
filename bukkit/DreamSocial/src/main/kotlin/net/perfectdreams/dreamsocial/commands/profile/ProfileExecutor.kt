package net.perfectdreams.dreamsocial.commands.profile

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.commands.options.buildSuggestionsBlockFromList
import net.perfectdreams.dreamsocial.DreamSocial
import net.perfectdreams.dreamsocial.dao.ProfileEntity
import net.perfectdreams.dreamsocial.gui.profile.renderProfileMenu
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.entity.Player

class ProfileExecutor(private val plugin: DreamSocial) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val target = optionalWord("player", buildSuggestionsBlockFromList {
            plugin.server.onlinePlayers.filterNot(DreamVanishAPI::isQueroTrabalhar).map(Player::getName)
        })
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val targetUsername = args[options.target]
        val isCheckingSelf = targetUsername == null || player.name.equals(targetUsername, true)

        plugin.schedule(SynchronizationContext.ASYNC) {
            val targetUUID =
                if (!isCheckingSelf) {
                    val userInfo = DreamUtils.retrieveUserInfoCaseInsensitive(targetUsername!!)
                        ?: return@schedule player.sendMessage("§c$targetUsername nunca jogou no SparklyPower.")

                    userInfo.id.value
                } else player.uniqueId

            val profileEntity = ProfileEntity.fetch(targetUUID)
            val renderedProfile = renderProfileMenu(this@ProfileExecutor.plugin, targetUUID, profileEntity.layout, isCheckingSelf)

            switchContext(SynchronizationContext.SYNC)

            renderedProfile.sendTo(player)
        }
    }
}