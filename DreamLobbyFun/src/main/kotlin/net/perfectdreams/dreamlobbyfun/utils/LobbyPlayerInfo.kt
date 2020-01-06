package net.perfectdreams.dreamlobbyfun.utils

import net.perfectdreams.dreamcore.pojo.PlayerInfo
import net.perfectdreams.libs.org.bson.codecs.pojo.annotations.BsonCreator
import net.perfectdreams.libs.org.bson.codecs.pojo.annotations.BsonProperty
import java.util.*

class LobbyPlayerInfo @BsonCreator constructor(
		@BsonProperty("_id")
		uniqueId: UUID,
		@BsonProperty("username")
		username: String,
		@BsonProperty("lowerCaseUsername")
		lowerCaseUsername: String
) : PlayerInfo(uniqueId, username, lowerCaseUsername) {
	var lobby = LobbyWrapper()
}
