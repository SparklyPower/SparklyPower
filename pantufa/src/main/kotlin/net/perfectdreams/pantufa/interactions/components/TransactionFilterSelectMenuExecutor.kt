package net.perfectdreams.pantufa.interactions.components

import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutor
import net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutorDeclaration
import net.perfectdreams.pantufa.interactions.components.utils.*
import net.perfectdreams.pantufa.pantufa

class TransactionFilterSelectMenuExecutor : SelectMenuExecutor {
    companion object : SelectMenuExecutorDeclaration("0002")

    override suspend fun onSelect(user: User, context: ComponentContext, values: List<String>) {
        val minecraftId = pantufa.retrieveDiscordAccountFromUser(user.id.value.toLong())?.minecraftId

        val decoded = context.data.decoded?.also {
            if (it.first.userId != user.id) return context.notForYou()
        } ?: return context.invalid()

        with (decoded.first) {
            showOnly = values.map(TransactionType::valueOf)
            context.updateMessage(buildTransactionsMessage(0, minecraftId))
        }
    }
}