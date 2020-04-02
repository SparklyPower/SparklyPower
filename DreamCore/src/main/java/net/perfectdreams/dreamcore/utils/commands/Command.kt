package net.perfectdreams.dreamcore.utils.commands

open class Command<T : CommandContext>(
    val labels: List<String>,
    val commandName: String,
    val executor: (T.() -> (Unit))
) {
    var permission: String? = null
}