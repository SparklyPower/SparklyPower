package net.perfectdreams.pantufa.api.commands

class CommandException(val reason: String, val prefix: String) : RuntimeException()