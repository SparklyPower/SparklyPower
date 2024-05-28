package net.perfectdreams.pantufa.utils.config

import kotlinx.serialization.Serializable

@Serializable
class PostgreSqlConfig(
    val databaseName: String,
    val ip: String,
    val port: Int,
    val username: String,
    val password: String
)