package net.perfectdreams.dreamcore.pojo

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import java.util.*

/*
  Plugins podem expandir a classe do PlayerInfo para adicionar as fields desejadas
 */
open class PlayerInfo @BsonCreator constructor(
		@BsonProperty("_id")
		@get:[BsonProperty("_id")]
		val uniqueId: UUID,
		@BsonProperty("username")
		@get:[BsonProperty("username")]
		val username: String,
		@BsonProperty("lowerCaseUsername")
		@get:[BsonProperty("lowerCaseUsername")]
		val lowerCaseUsername: String
)