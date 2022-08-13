package net.perfectdreams.cookedsql

import com.github.michaelbull.jdbc.context.CoroutineDataSource
import com.github.michaelbull.jdbc.context.connection
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import kotlin.coroutines.coroutineContext

/**
 * A tiny wrapper on top of JDBC
 */
class CookedSQL(dataSource: HikariDataSource) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val scope = CoroutineScope(Dispatchers.IO + CoroutineDataSource(dataSource))

    suspend fun <T> transaction(repetitions: Int = 5, action: suspend CoroutineScope.(connection: CookedSQLConnection) -> (T)): T {
        var lastException: Throwable? = null
        for (i in 1..repetitions) {
            try {
                return withContext((coroutineContext + scope.coroutineContext)) {
                    com.github.michaelbull.jdbc.transaction {
                        action.invoke(this, CookedSQLConnection(this.coroutineContext.connection))
                    }
                }
            } catch (e: Throwable) {
                logger.warn(e) { "Exception while trying to execute query. Tries: $i" }
                lastException = e
            }
        }
        throw lastException ?: RuntimeException("This should never happen")
    }
}