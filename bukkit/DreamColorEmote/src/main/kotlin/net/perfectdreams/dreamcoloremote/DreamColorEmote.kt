package net.perfectdreams.dreamcoloremote

import net.perfectdreams.dreamcoloremote.commands.ColorsCommand
import net.perfectdreams.dreamcoloremote.commands.EmoteListCommand
import net.perfectdreams.dreamcore.utils.KotlinPlugin

class DreamColorEmote : KotlinPlugin() {

    override fun softEnable() {
        super.softEnable()

        registerCommand(EmoteListCommand)
        registerCommand(ColorsCommand)
    }
}