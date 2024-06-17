package net.perfectdreams.pantufa.api.commands.exceptions

import dev.minn.jda.ktx.messages.InlineMessage

class CommandException(val ephemeral: Boolean, val builder: InlineMessage<*>.() -> (Unit)) : RuntimeException()
