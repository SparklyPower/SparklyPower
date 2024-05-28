package net.perfectdreams.pantufa.utils

import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.postgresql.jdbc.PgConnection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PostgreSQLNotificationListener(
    private val hikariDataSource: HikariDataSource,
    private val callbacks: Map<String, (String) -> (Unit)>,
    private val validationTimeout: Duration = 3.seconds,
    private val notificationsTimeout: Duration? = 60.seconds
) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        // Validations
        if (validationTimeout.inWholeSeconds.toInt() == 0)
            logger.warn { "Validation Timeout is set to 0, if you aren't using your data source to execute queries, this will cause issues if your connection to PostgreSQL experiences a network failure because your connection will never know that the connection is inactive! Consider increasing the validation timeout to avoid this bug." }

        if (notificationsTimeout == null)
            logger.warn { "Notifications timeout is unset, this will cause the \"getNotifications\" call to block until a notification is received, however, this will cause issues if your connection to PostgreSQL experiences a network failure because your connection will never know that the connection is inactive! Consider increasing the notifications timeout to avoid this bug." }
    }

    override fun run() {
        logger.info { "Starting notification listener..." }
        while (true) {
            logger.info { "Within the while (true) for notifications loop..." }
            try {
                hikariDataSource.connection
                    .use {
                        // Unwrap must be within the ".use" block to avoid connection leaks when PostgreSQL goes down!
                        val pgConnection = it.unwrap(PgConnection::class.java)

                        val stmt = pgConnection.createStatement()
                        for (channel in callbacks.keys) {
                            stmt.execute("LISTEN $channel;")
                        }
                        stmt.close()
                        pgConnection.commit()

                        while (pgConnection.isValid(validationTimeout.inWholeSeconds.toInt())) { // Validate if the connection is still alive to avoid network issues - https://github.com/pgjdbc/pgjdbc/issues/1144#issuecomment-1219818764
                            logger.info { "Querying for notifications..." }
                            // 0 == blocks forever
                            val notifications = pgConnection.getNotifications(notificationsTimeout?.inWholeMilliseconds?.toInt() ?: 0)

                            for (notification in notifications) {
                                try {
                                    val callback = callbacks[notification.name]
                                    callback?.invoke(notification.parameter)
                                } catch (e: Exception) {
                                    logger.warn(e) { "Something went wrong while processing PostgreSQL notification ${notification.name}! We will ignore it..." }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while listening for PostgreSQL notifications! We will restart the connection..." }
            }

            logger.warn { "Left the notification connection block, this may mean that the connection is dead! Trying to reconnect..." }
        }
    }
}