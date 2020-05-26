package net.perfectdreams.dreamchat.utils

import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.bukkit.event.player.AsyncPlayerChatEvent

object McMMOTagsUtils {
    fun addTags(e: AsyncPlayerChatEvent, playerTagsEvent: ApplyPlayerTagsEvent) {
        val nothingOrArtigo = if (e.player.girl) "a" else ""

        if (DreamChat.INSTANCE.topMcMMOPlayer.equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§6§lT",
                    "§6§lTop Poder",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 do nível de poder do McMMO!"
                    ),
                    "/mctop",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.ACROBATICS].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lAc",
                    "§e§lAcrobátic${e.player.girl}",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 de acrobacia do McMMO!"
                    ),
                    "/mctop acrobacia",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.ALCHEMY].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lAl",
                    "§e§lAlquimista",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 de alquimia do McMMO!"
                    ),
                    "/mctop alquimia",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.ARCHERY].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lAr",
                    "§e§lArqueir${e.player.artigo}",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 arqueiro do McMMO!"
                    ),
                    "/mctop arco",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.AXES].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lM",
                    "§e§lMachados",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 de machados do McMMO!"
                    ),
                    "/mctop machados",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.EXCAVATION].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lEsc",
                    "§e§lEscavador$nothingOrArtigo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 escavador do McMMO!"
                    ),
                    "/mctop escavacao",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.FISHING].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lP",
                    "§e§lPescador$nothingOrArtigo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 pescador do McMMO!"
                    ),
                    "/mctop pesca",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.HERBALISM].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lH",
                    "§e§lHerbalismo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 de herbalismo do McMMO!"
                    ),
                    "/mctop herbalismo",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.MINING].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lM",
                    "§e§lMinerador$nothingOrArtigo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 minerador do McMMO!"
                    ),
                    "/mctop mineracao",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.REPAIR].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lR",
                    "§e§lReparador$nothingOrArtigo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 reparador do McMMO!"
                    ),
                    "/mctop reparacao",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.SWORDS].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lEsp",
                    "§e§lEspadas",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 espadas do McMMO!"
                    ),
                    "/mctop espadas",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.TAMING].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lV",
                    "§e§lVeterinári${e.player.artigo}",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 de domesticar animais do McMMO!"
                    ),
                    "/mctop domesticar",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.UNARMED].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lD",
                    "§e§lDesarmad${e.player.artigo}",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 desarmado do McMMO!"
                    ),
                    "/mctop desarmado",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.WOODCUTTING].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§e§lL",
                    "§e§lLenhador$nothingOrArtigo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 lenhador do McMMO!"
                    ),
                    "/mctop lenhador",
                    false
                )
            )
        }
    }
}