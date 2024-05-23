package net.perfectdreams.minecraftmojangapi

import io.ktor.http.*

class MinecraftMojangAPIException(val statusCode: HttpStatusCode) : RuntimeException()