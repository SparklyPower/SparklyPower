package net.perfectdreams.dreamcorebungee.utils.extensions

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent

operator fun BaseComponent.plusAssign(baseComponent: BaseComponent) {
	this.addExtra(baseComponent)
}

operator fun BaseComponent.plusAssign(str: String) {
	this.addExtra(str)
}

operator fun BaseComponent.plusAssign(baseComponents: Array<out BaseComponent>) {
	for (baseComponent in baseComponents) {
		this.addExtra(baseComponent)
	}
}

fun String.toBaseComponent(): Array<out BaseComponent> {
	return TextComponent.fromLegacyText(this)
}

fun String.toTextComponent(): TextComponent {
	return TextComponent(*TextComponent.fromLegacyText(this))
}