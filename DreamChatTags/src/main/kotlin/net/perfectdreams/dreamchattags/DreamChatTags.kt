package net.perfectdreams.dreamchattags

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamchattags.listeners.TagListener
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Particle
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamChatTags : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(TagListener(this))
		registerCommand(command("DreamChatTagsCommand", listOf("dreamchattags")) {
			permission = "dreamchattags.setup"

			executes {
				this@DreamChatTags.reloadConfig()
				sender.sendMessage("Recarregado com sucesso!")
			}
		})
	}

	override fun softDisable() {
		super.softDisable()
	}
}