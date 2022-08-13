package net.perfectdreams.cookedsql

import java.sql.ResultSet

class CookedSQLResultSet(val resultSet: ResultSet) {
    fun first(): ResultSet {
        while (resultSet.next())
            return resultSet

        throw NoSuchElementException()
    }

    fun firstOrNull(): ResultSet? {
        while (resultSet.next())
            return resultSet

        return null
    }
}