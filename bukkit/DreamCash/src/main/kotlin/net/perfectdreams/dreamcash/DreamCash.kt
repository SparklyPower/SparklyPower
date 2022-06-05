package net.perfectdreams.dreamcash

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreamcash.commands.DreamCashCommand
import net.perfectdreams.dreamcash.commands.LojaCashCommand
import net.perfectdreams.dreamcash.tables.Cashes
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DreamCash : KotlinPlugin() {
    override fun softEnable() {
        super.softEnable()

        registerCommand(DreamCashCommand(this))
        registerCommand(LojaCashCommand(this))

        transaction(Databases.databaseNetwork) {
            SchemaUtils.create(Cashes)
        }

        val bedrockIntegrations = Bukkit.getPluginManager().getPlugin("DreamBedrockIntegrations") as DreamBedrockIntegrations
        bedrockIntegrations.registerInventoryTitleTransformer(
            this,
            { PlainTextComponentSerializer.plainText().serialize(it).contains("\uE261") },
            { Component.text("A Loja de seus ")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .append(
                    Component.text("Pesadelos")
                        .color(NamedTextColor.RED)
                )
            }
        )
    }

    companion object {
        const val PREFIX = "§8[§c§lPesadelos§8]§e"
    }
}