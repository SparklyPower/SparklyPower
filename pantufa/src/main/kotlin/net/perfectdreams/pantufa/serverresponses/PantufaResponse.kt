package net.perfectdreams.pantufa.serverresponses

import net.dv8tion.jda.api.entities.User

interface PantufaResponse {
    /**
     * Priority is used to avoid responses overlapping another
     * Example: When a response is more "open" and can match a lot of variations, while another response is more "closed" and is more specific.
     *
     * Responses are sorted by higher priority -> lower priority
     */
    val priority: Int
        get() = 0

    /**
     * Checks if this response matches the [message]
     *
     * @see handleResponse
     * @return if the response matches the question
     */
    fun handleResponse(message: String): Boolean

    /**
     * Gets all the [PantufaReply] messages of this response
     *
     * @return a list (can be empty) of [LorittaReply] of this response
     */
    fun getSupportResponse(author: User, message: String): AutomatedSupportResponse?
}