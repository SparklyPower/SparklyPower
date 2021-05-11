package net.perfectdreams.dreamcore.utils.commands.annotation

@Deprecated(message = "Please use the new command framework")
annotation class SubcommandPermission(val permission: String, val message: String = "{UseDefaultMessage}", val callbackName: String = "")