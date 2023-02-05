package net.perfectdreams.dreamcore.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object PreferencesTable : UUIDTable() {
    val seePrivateMessages = bool("see_private_messages").default(true)
    val seePlayersAnnouncements = bool("see_players_announcements").default(true)
    val seeServerAnnouncements = bool("see_server_announcements").default(true)
    val seeEventsAnnouncements = bool("see_events_announcements").default(true)
    val seeLoginAnnouncements = bool("see_login_announcements").default(true)
    val seeJetpackMessages = bool("see_jetpack_messages").default(true)
    val seeGamblingMessages = bool("see_gambling_messages").default(true)
    val seeVotesMessages = bool("see_votes_messages").default(true)
    val seeChatEvents = bool("see_chat_events").default(true)
    val seeThanosSnap = bool("see_thanos_snap").default(true)
}