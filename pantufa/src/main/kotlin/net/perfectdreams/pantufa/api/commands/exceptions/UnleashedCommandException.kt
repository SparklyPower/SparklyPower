package net.perfectdreams.pantufa.api.commands.exceptions

import dev.minn.jda.ktx.messages.InlineMessage

class UnleashedCommandException(val ephemeral: Boolean, val builder: InlineMessage<*>.() -> (Unit)) : RuntimeException()
