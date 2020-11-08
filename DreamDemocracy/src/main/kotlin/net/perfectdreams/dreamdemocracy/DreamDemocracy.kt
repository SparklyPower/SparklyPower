package net.perfectdreams.dreamdemocracy

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreammusically.listeners.PlayerListener
import net.perfectdreams.dreammusically.utils.MusicPack
import net.perfectdreams.dreammusically.utils.Song
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class DreamDemocracy : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(PlayerListener(this))
	}
}