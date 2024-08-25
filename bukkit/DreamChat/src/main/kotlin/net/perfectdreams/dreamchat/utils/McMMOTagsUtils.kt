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
                    "§x§3§f§f§8§f§f§lAc",
                    "§x§3§f§f§8§f§f§lAcrobátic${e.player.artigo}",
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
                    "§x§d§7§3§0§c§1§lAl",
                    "§x§d§7§3§0§c§1§lAlquimista",
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
                    "§x§2§9§d§5§8§9§lAr",
                    "§x§2§9§d§5§8§9§lArqueir${e.player.artigo}",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 arqueiro do McMMO!"
                    ),
                    "/mctop arquearia",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.AXES].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§x§f§b§4§f§4§f§lV",
                    "§x§f§b§4§f§4§f§lViking",
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
                    "§x§c§f§a§b§5§7§lEsc",
                    "§x§c§f§a§b§5§7§lEscavador$nothingOrArtigo",
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
                    "§x§2§e§7§b§c§c§lP",
                    "§x§2§e§7§b§c§c§lPescador$nothingOrArtigo",
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
                    "§x§3§9§a§e§2§2§lH",
                    "§x§3§9§a§e§2§2§lHerbalismo",
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
                    "§x§8§7§8§8§8§2§lM",
                    "§x§8§7§8§8§8§2§lMinerador$nothingOrArtigo",
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
                    "§x§f§2§f§f§1§6§lR",
                    "§x§f§2§f§f§1§6§lReparador$nothingOrArtigo",
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
                    "§x§d§e§5§3§a§d§lEsp",
                    "§x§d§e§5§3§a§d§lEspadachim",
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
                    "§x§4§e§b§1§9§d§lV",
                    "§x§4§e§b§1§9§d§lVeterinári${e.player.artigo}",
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
                    "§x§c§2§c§d§1§4§lB",
                    "§x§c§2§c§d§1§4§lBoxeador${nothingOrArtigo}",
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
                    "§x§f§f§a§c§3§c§lL",
                    "§x§f§f§a§c§3§c§lLenhador$nothingOrArtigo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 lenhador do McMMO!"
                    ),
                    "/mctop lenhador",
                    false
                )
            )
        }

        //Clava
        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.MACES].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§x§7§0§8§0§9§0§lD",
                    "§x§7§0§8§0§9§0§lDemolidor",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 mangual do McMMO!"
                    ),
                    "/mctop mangual",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.TRIDENTS].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§x§0§0§0§0§f§f§lP",
                    "§x§0§0§0§0§f§f§lPoseidon",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 com tridentes do McMMO!"
                    ),
                    "/mctop tridentes",
                    false
                )
            )
        }

        if (DreamChat.INSTANCE.topPlayerSkills[PrimarySkillType.CROSSBOWS].equals(e.player.name, true)) {
            playerTagsEvent.tags.add(
                PlayerTag(
                    "§x§f§f§f§f§0§0§lA",
                    "§x§f§f§f§f§0§0§lAtirador$nothingOrArtigo",
                    listOf(
                        "§b${e.player.displayName}§7 é o top #1 atirador com bestas do McMMO!"
                    ),
                    "/mctop besta",
                    false
                )
            )
        }
    }
}