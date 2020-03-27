package net.perfectdreams.dreamclubes.utils

enum class ClubePermissionLevel(val tagName: String, val weight: Int) {
    OWNER("DONO", 999),
    ADMIN("ADMIN", 1),
    MEMBER("MEMBRO", 0);

    fun canExecute(permission: ClubePermissionLevel): Boolean {
        return this.weight >= permission.weight
    }
}