package net.perfectdreams.dreamvote.utils

import org.bukkit.inventory.ItemStack

class VoteAward {
	var name: String = "???"
	var items = mutableListOf<ItemStack>()
	var money = 0
	var hidden = false
	var hasEqualsVoteCountCondition = false
	var requiredEqualsVoteCount: Int = 0
}