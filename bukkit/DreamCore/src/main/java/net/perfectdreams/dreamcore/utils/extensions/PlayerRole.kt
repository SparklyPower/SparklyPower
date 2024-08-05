package net.perfectdreams.dreamcore.utils.extensions

import org.bukkit.entity.Player

enum class PlayerRole(val localizedName: String, val permission: String) {
    MEMBER("Membro", "group.default"),
    VIP("VIP", "group.vip"),
    VIP_PLUS("VIP+", "group.vip+"),
    VIP_PLUS_PLUS("VIP++", "group.vip++"),
    DEVELOPER("Desenvolvedor", "group.developer"),
    BUILDER("Construtor", "group.builder"),
    TRIAL_SUPPORT("Trial Suporte", "group.trialsuporte"),
    SUPPORT("Suporte", "group.suporte"),
    MODERATOR("Moderador", "group.moderador"),
    ADMIN("Admin", "group.admin"),
    OWNER("Dono", "group.dono");

    fun isPlayerInIt(player: Player) = player.hasPermission(this.permission)

    companion object {
        private val values = entries.associateBy(PlayerRole::permission)

        fun getFromPermission(permission: String) = values[permission] ?: MEMBER
    }
}

val Player.isVIP get() = this.hasPermission("group.vip")

val Player.isVIPPlus get() = this.hasPermission("group.vip+")

val Player.isVIPPlusPlus get() = this.hasPermission("group.vip++")

val Player.hasAnyVIP get() = this.isVIP || this.isVIPPlus || this.isVIPPlusPlus

val Player.isStaff get() = this.hasPermission("sparklypower.soustaff")

val Player.highestRole get() = PlayerRole.entries.lastOrNull { it.isPlayerInIt(this) } ?: PlayerRole.MEMBER