package net.sparklypower.sparklyneonvelocity.utils

fun Boolean.prettyBoolean(): String {
	return if (this) {
		"§a§lSIM"
	} else {
		"§4§lNÃO"
	}
}