package net.perfectdreams.dreamxizum.battle

enum class BattleItems {
    CUSTOM_ITEMS,
    PLUGIN_KITS,
    PLAYER_ITEMS
}

enum class BattleStage {
    CREATING_BATTLE,
    WAITING_PLAYERS,
    COUNTDOWN,
    FIGHTING,
    FINISHED
}

enum class BattleType {
    NORMAL,
    RANKED
}

enum class BattleUserStatus {
    PENDING,
    ALIVE,
    DECEASED,
    LEFT
}

enum class BattleDeathReason {
    KILLED,
    DISCONNECTED,
    TELEPORTED
}