package net.perfectdreams.pantufa.interactions.components

import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.components.*
import net.perfectdreams.pantufa.interactions.components.utils.*
import net.perfectdreams.pantufa.pantufa

class ChangePageButtonClickExecutor : ButtonExecutor {
    companion object : ButtonExecutorDeclaration("0001")

    override suspend fun onClick(user: User, context: ComponentContext) {
        val minecraftId = pantufa.retrieveDiscordAccountFromUser(user.id.value.toLong())?.minecraftId

        val decoded = context.data.decoded?.also {
            if (it.first.userId != user.id) return context.notForYou()
        } ?: return context.invalid()

        val messageData = decoded.first
        val page = decoded.second

        context.updateMessage(
            if (messageData.type == MessagePanelType.TRANSACTIONS)
                messageData.buildTransactionsMessage(page, minecraftId)
            else
                messageData.buildCommandsLogMessage(page)
        )
    }
}