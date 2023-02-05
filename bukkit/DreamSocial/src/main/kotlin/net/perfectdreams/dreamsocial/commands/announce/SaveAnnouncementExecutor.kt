package net.perfectdreams.dreamsocial.commands.announce

import com.github.benmanes.caffeine.cache.Caffeine
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.text.format.TextColor
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamcore.utils.minutes
import net.perfectdreams.dreamsocial.DreamSocial
import net.perfectdreams.dreamsocial.dao.AnnouncementsEntity
import net.perfectdreams.dreamsocial.gui.announcement.renderAnnouncementsMenu
import org.bukkit.entity.Player

class SaveAnnouncementExecutor(private val plugin: DreamSocial) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val canPlayerSaveAnnouncements = player.highestRole >= PlayerRole.VIP_PLUS

        if (!canPlayerSaveAnnouncements) context.fail(MUST_BE_VIP_PLUS.asComponent.color(RED_COLOR))

        lastAnnouncements[player]?.let {
            plugin.schedule(SynchronizationContext.ASYNC) {
                val savedAnnouncements = AnnouncementsEntity.fetch(player.uniqueId)

                switchContext(SynchronizationContext.SYNC)

                renderAnnouncementsMenu(this@SaveAnnouncementExecutor.plugin, player, savedAnnouncements, it).sendTo(player)
            }
        } ?: context.fail(NOT_FOUND.asComponent.color(RED_COLOR))
    }

    companion object {
        private const val MUST_BE_VIP_PLUS = "Desculpa, mas você precisa ter no mínimo VIP+ para poder usufruir deste benefício."
        private const val NOT_FOUND = "Hmm, não encontrei nenhum anúncio recente que seja seu. Que tal fazer um novo agora?"

        private val RED_COLOR = TextColor.color(0xFF5555)

        val lastAnnouncements = Caffeine.newBuilder()
            .expireAfterWrite(15.minutes)
            .build<Player, String>()
            .asMap()
    }

}