package net.perfectdreams.dreamcore.utils.exposed

import com.google.gson.Gson
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

fun <T : Any> Table.jsonb(name: String, klass: Class<T>, jsonMapper: Gson): Column<T>
		= registerColumn(name, JsonColumnType(klass, jsonMapper))

class JsonColumnType<out T : Any>(private val klass: Class<T>, private val jsonMapper: Gson) : ColumnType() {
	override fun sqlType() = "jsonb"

	override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
		val obj = PGobject()
		obj.type = "jsonb"
		obj.value = value as String
		stmt.setObject(index, obj)
	}

	override fun valueFromDB(value: Any): Any {
		value as PGobject
		return try {
			jsonMapper.fromJson(value.value, klass)
		} catch (e: Exception) {
			e.printStackTrace()
			throw RuntimeException("Can't parse JSON: $value")
		}
	}

	override fun notNullValueToDB(value: Any): Any = jsonMapper.toJson(value)
	override fun nonNullValueToString(value: Any): String = "'${jsonMapper.toJson(value)}'"
}
