package net.perfectdreams.dreamcore.dao

import net.perfectdreams.dreamcore.tables.PreferencesTable
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class PreferencesEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PreferencesEntity>(PreferencesTable) {
        fun fetch(uuid: UUID) = transaction(Databases.databaseNetwork) {
            findById(uuid) ?: new(uuid) {}
        }
    }

    private var seePrivateMessages by PreferencesTable.seePrivateMessages
    private var seePlayersAnnouncements by PreferencesTable.seePlayersAnnouncements
    private var seeServerAnnouncements by PreferencesTable.seeServerAnnouncements
    private var seeEventsAnnouncements by PreferencesTable.seeEventsAnnouncements
    private var seeLoginAnnouncements by PreferencesTable.seeLoginAnnouncements
    private var seeJetpackMessages by PreferencesTable.seeJetpackMessages
    private var seeGamblingMessages by PreferencesTable.seeGamblingMessages
    private var seeVotesMessages by PreferencesTable.seeVotesMessages
    private var seeChatEvents by PreferencesTable.seeChatEvents
    private var seeThanosSnap by PreferencesTable.seeThanosSnap

    operator fun get(broadcastType: BroadcastType) = when(broadcastType) {
        BroadcastType.PRIVATE_MESSAGE -> seePrivateMessages
        BroadcastType.PLAYER_ANNOUNCEMENT -> seePlayersAnnouncements
        BroadcastType.SERVER_ANNOUNCEMENT -> seeServerAnnouncements
        BroadcastType.EVENT_ANNOUNCEMENT -> seeEventsAnnouncements
        BroadcastType.LOGIN_ANNOUNCEMENT -> seeLoginAnnouncements
        BroadcastType.JETPACK_MESSAGE -> seeJetpackMessages
        BroadcastType.GAMBLING_MESSAGE -> seeGamblingMessages
        BroadcastType.VOTES_MESSAGE -> seeVotesMessages
        BroadcastType.CHAT_EVENT -> seeChatEvents
        BroadcastType.THANOS_SNAP -> seeThanosSnap
    }

    fun flip(broadcastType: BroadcastType) = transaction(Databases.databaseNetwork) {
        when (broadcastType) {
            BroadcastType.PRIVATE_MESSAGE -> seePrivateMessages = !seePrivateMessages
            BroadcastType.PLAYER_ANNOUNCEMENT -> seePlayersAnnouncements = !seePlayersAnnouncements
            BroadcastType.SERVER_ANNOUNCEMENT -> seeServerAnnouncements = !seeServerAnnouncements
            BroadcastType.EVENT_ANNOUNCEMENT -> seeEventsAnnouncements = !seeEventsAnnouncements
            BroadcastType.LOGIN_ANNOUNCEMENT -> seeLoginAnnouncements = !seeLoginAnnouncements
            BroadcastType.JETPACK_MESSAGE -> seeJetpackMessages = !seeJetpackMessages
            BroadcastType.GAMBLING_MESSAGE -> seeGamblingMessages = !seeGamblingMessages
            BroadcastType.VOTES_MESSAGE -> seeVotesMessages = !seeVotesMessages
            BroadcastType.CHAT_EVENT -> seeChatEvents = !seeChatEvents
            BroadcastType.THANOS_SNAP -> seeThanosSnap = !seeThanosSnap
        }
    }
}