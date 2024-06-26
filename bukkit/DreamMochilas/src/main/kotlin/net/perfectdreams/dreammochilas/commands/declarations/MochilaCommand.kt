package net.perfectdreams.dreammochilas.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.commands.*

class MochilaCommand(val m: DreamMochilas) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("mochila")) {
        permissions = listOf("sparklymochilas.setup")

        subcommand(listOf("get")) {
            permissions = listOf("sparklymochilas.setup")
            executor = GetMochilaExecutor()
        }

        subcommand(listOf("get_old")) {
            permissions = listOf("sparklymochilas.setup")
            executor = GetMochilaOldDamageSystemExecutor()
        }

        subcommand(listOf("id")) {
            permissions = listOf("sparklymochilas.setup")
            executor = GetMochilaIdExecutor()
        }

        subcommand(listOf("get_by_id")) {
            permissions = listOf("sparklymochilas.setup")
            executor = GetMochilaByIdExecutor(m)
        }

        subcommand(listOf("player")) {
            permissions = listOf("sparklymochilas.setup")
            executor = GetPlayerMochilasExecutor()
        }

        subcommand(listOf("memory_mochilas")) {
            permissions = listOf("sparklymochilas.setup")
            executor = MochilasMemoryExecutor(m)
        }

        subcommand(listOf("clearlocks")) {
            permissions = listOf("sparklymochilas.setup")
            executor = ClearMochilasLockExecutor(m)
        }

        subcommand(listOf("hack_tests")) {
            subcommand(listOf("interact_and_open")) {
                permissions = listOf("sparklymochilas.setup")
                executor = FakeInteractAndOpenExecutor(m)
            }

            subcommand(listOf("interact_auto_click")) {
                permissions = listOf("sparklymochilas.setup")
                executor = FakeInteractAutoClickExecutor(m)
            }
        }
    }
}