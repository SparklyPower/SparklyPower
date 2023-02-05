package net.perfectdreams.dreamsocial.tables

import net.perfectdreams.dreamsocial.gui.profile.ProfileLayout
import org.jetbrains.exposed.dao.id.UUIDTable

object ProfilesTable : UUIDTable() {
    val layout = enumeration<ProfileLayout>("layout").clientDefault { ProfileLayout.freeToUseLayouts.random() }
}