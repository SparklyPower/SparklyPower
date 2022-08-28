package net.perfectdreams.dreamclubes.commands.declarations

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.ClubeChatExecutor
import net.perfectdreams.dreamclubes.commands.ClubesExecutor
import net.perfectdreams.dreamclubes.commands.subcommands.*
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand

class ClubesCommand(val m: DreamClubes) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("clube", "clubes", "clan", "clans")) {
        // ===[ CLUBE MANAGEMENT ]===
        subcommand(listOf("admin")) {
            executor = AdminClubeExecutor(m)
        }

        subcommand(listOf("owner", "dono")) {
            executor = DonoClubeExecutor(m)
        }

        subcommand(listOf("tag")) {
            executor = TagClubeExecutor(m)
        }

        subcommand(listOf("name", "nome")) {
            executor = NameClubeExecutor(m)
        }

        subcommand(listOf("players")) {
            executor = PlayersClubeExecutor(m)
        }

        subcommand(listOf("tag")) {
            executor = TagClubeExecutor(m)
        }

        subcommand(listOf("coords")) {
            executor = CoordsClubeExecutor(m)
        }

        subcommand(listOf("convidar")) {
            executor = ConvidarClubeExecutor(m)
        }

        subcommand(listOf("playertag", "prefixo")) {
            executor = PlayerTagClubeExecutor(m)
        }

        subcommand(listOf("vitals")) {
            executor = VitalsClubeExecutor(m)
        }

        subcommand(listOf("kick", "expulsar")) {
            executor = KickClubeExecutor(m)
        }

        subcommand(listOf("sair", "quit")) {
            executor = SairClubeExecutor(m)
        }

        subcommand(listOf("delete", "deletar")) {
            executor = DeletarClubeExecutor(m)
        }

        subcommand(listOf("sair", "quit")) {
            executor = SairClubeExecutor(m)
        }

        subcommand(listOf("casa", "home")) {
            executor = HomeClubeExecutor(m)
        }

        subcommand(listOf("setcasa", "sethome")) {
            executor = SetHomeClubeExecutor(m)
        }

        // ===[ ACCEPT INVITE ]===
        subcommand(listOf("aceitar", "accept")) {
            executor = AceitarClubeExecutor(m)
        }

        // ===[ CREATE CLUBE ]===
        subcommand(listOf("criar", "create")) {
            executor = CreateClubeExecutor(m)
        }

        executor = ClubesExecutor(m)
    }
}