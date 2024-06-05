package net.perfectdreams.dreamsocial.gui.profile.helper

import com.gmail.nossr50.datatypes.skills.PrimarySkillType

val alphabeticallySortedList = PrimarySkillType.entries.sortedBy(PrimarySkillType::localizedName)

val PrimarySkillType.localizedName get() = when(this) {
    PrimarySkillType.ACROBATICS -> "Acrobacia"
    PrimarySkillType.ALCHEMY -> "Alquimia"
    PrimarySkillType.ARCHERY -> "Arquearia"
    PrimarySkillType.AXES -> "Machados"
    PrimarySkillType.EXCAVATION -> "Escavação"
    PrimarySkillType.FISHING -> "Pesca"
    PrimarySkillType.HERBALISM -> "Herbalismo"
    PrimarySkillType.MINING -> "Mineração"
    PrimarySkillType.REPAIR -> "Reparação"
    PrimarySkillType.SALVAGE -> "Recuperação"
    PrimarySkillType.SMELTING -> "Fundição"
    PrimarySkillType.SWORDS -> "Espadas"
    PrimarySkillType.TAMING -> "Adestramento"
    PrimarySkillType.UNARMED -> "Desarmado"
    PrimarySkillType.WOODCUTTING -> "Lenhador"
    PrimarySkillType.CROSSBOWS -> "Crossbows"
    PrimarySkillType.MACES -> "Maces"
    PrimarySkillType.TRIDENTS -> "Tridents"
}