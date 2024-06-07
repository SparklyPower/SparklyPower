package net.perfectdreams.dreamcore.utils

import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RegisterRecipesOnReloadListener(val m: KotlinPlugin) : Listener {
    // Workaround while Paper does not have a LifecycleEvents.RECIPES
    // https://canary.discord.com/channels/289587909051416579/555462289851940864/1248108887396061305
    @EventHandler
    fun onReload(event: ServerResourcesReloadedEvent) {
        if (m.recipes.isNotEmpty()) {
            m.logger.info("Reregistering recipes because the server was reloaded!")
            for (recipe in m.recipes) {
                Bukkit.addRecipe(recipe.value)
            }
        }
    }
}