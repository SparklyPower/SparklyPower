package net.perfectdreams.dreamcore.utils.preferences

import net.kyori.adventure.text.TextComponent
import net.md_5.bungee.api.chat.BaseComponent
import net.perfectdreams.dreamcore.utils.extensions.PlayerRole
import net.perfectdreams.dreamcore.utils.extensions.asBoldComponent
import net.perfectdreams.dreamcore.utils.extensions.highestRole
import org.bukkit.Bukkit
import org.bukkit.entity.Player

enum class BroadcastType(private val localizedName: String, val minimumRoleToDisable: PlayerRole = PlayerRole.MEMBER) {
    PRIVATE_MESSAGE("mensagens privadas"),
    PLAYER_ANNOUNCEMENT("anúncios de jogadores", PlayerRole.VIP),
    SERVER_ANNOUNCEMENT("anúncios do servidor"),
    EVENT_ANNOUNCEMENT("anúncios de eventos"),
    LOGIN_ANNOUNCEMENT("anúncios de login"),
    JETPACK_MESSAGE("mensagens de jetpack"),
    GAMBLING_MESSAGE("mensagens de aposta"),
    VOTES_MESSAGE("mensagens de votação"),
    CHAT_EVENT("eventos chat"),
    THANOS_SNAP("thanos snap");

    val componentName = (this.localizedName.replaceFirstChar(Char::uppercase) +
            if (minimumRoleToDisable > PlayerRole.MEMBER) " (Requer ${minimumRoleToDisable.localizedName})" else "").asBoldComponent
}

fun Player.shouldSeeBroadcast(broadcastType: BroadcastType): Boolean {
    val shouldSeeIt = PreferencesManager.getPlayerPreferences(this)?.get(broadcastType) ?: true
    val canPlayerDisableIt = this.highestRole >= broadcastType.minimumRoleToDisable

    return !(canPlayerDisableIt && !shouldSeeIt)
}

fun Player.sendMessage(message: String, broadcastType: BroadcastType) {
    if (this.shouldSeeBroadcast(broadcastType)) this.sendMessage(message)
}

fun Player.sendMessage(message: TextComponent, broadcastType: BroadcastType) {
    if (this.shouldSeeBroadcast(broadcastType)) this.sendMessage(message)
}

fun Player.sendMessage(message: net.md_5.bungee.api.chat.TextComponent, broadcastType: BroadcastType) {
    if (this.shouldSeeBroadcast(broadcastType)) this.sendMessage(message)
}

fun Player.sendMessage(message: BaseComponent, broadcastType: BroadcastType) {
    if (this.shouldSeeBroadcast(broadcastType)) this.sendMessage(message)
}

fun broadcastMessage(broadcastType: BroadcastType, message: (Player) -> String) {
    Bukkit.getOnlinePlayers().forEach {
        it.sendMessage(message.invoke(it), broadcastType)
    }
}

fun broadcastMessage(broadcastType: BroadcastType, message: TextComponent) {
    Bukkit.getOnlinePlayers().forEach {
        it.sendMessage(message, broadcastType)
    }
}

fun broadcastMessage(broadcastType: BroadcastType, message: net.md_5.bungee.api.chat.TextComponent) {
    Bukkit.getOnlinePlayers().forEach {
        it.sendMessage(message, broadcastType)
    }
}

fun broadcastMessage(broadcastType: BroadcastType, message: BaseComponent) {
    Bukkit.getOnlinePlayers().forEach {
        it.sendMessage(message, broadcastType)
    }
}