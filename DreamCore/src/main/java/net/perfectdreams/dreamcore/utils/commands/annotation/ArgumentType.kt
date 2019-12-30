package net.perfectdreams.dreamcore.utils.commands.annotation

@Deprecated(message = "Please use the new command framework")
enum class ArgumentType {
	PLAYER,
	PLAYER_EXACT,
	@Deprecated("Please use ALL_ARGUMENTS_AS_STRING")
	ALL_ARGUMENTS_LIST,
	@Deprecated("Please use ARGUMENTS_AS_STRING")
	ARGUMENT_LIST,
	ALL_ARGUMENTS_AS_STRING,
	ARGUMENTS_AS_STRING,
	ALL_ARGUMENTS_ARRAY,
	WORLD,
	COMMAND_LABEL,
	CUSTOM,
	CUSTOM_ARGUMENT
}