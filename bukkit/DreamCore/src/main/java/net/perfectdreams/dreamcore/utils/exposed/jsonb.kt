package net.perfectdreams.dreamcore.utils.exposed

import com.google.gson.Gson
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import java.sql.ResultSet


fun <T : Any> Table.jsonb(name: String, klass: Class<T>, jsonMapper: Gson): Column<T>
		= registerColumn(name, Json(klass, jsonMapper))

class Json<T : Any>(private val klass: Class<T>, private val jsonMapper: Gson) : ColumnType<T>() {
	override fun sqlType() = "jsonb"

	override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
		val obj = PGobject()
		obj.type = "jsonb"
		obj.value = value as String
		stmt[index] = obj
	}

	override fun valueFromDB(value: Any): T {
		if (value !is PGobject)
			return value as T

		return try {
			jsonMapper.fromJson(value.value, klass)
		} catch (e: Exception) {
			e.printStackTrace()
			throw RuntimeException("Can't parse JSON: $value")
		}
	}

	override fun notNullValueToDB(value: T): Any {
		if (value is String)
			return value
		return jsonMapper.toJson(value)
	}

	override fun nonNullValueToString(value: T): String = "'${jsonMapper.toJson(value)}'"
}

// From ExposedPowerUtils but updated to work with Exposed 0.50.1
class JsonBinary : ColumnType<String>() {
	override fun sqlType() = "JSONB"

	override fun valueFromDB(value: Any): String {
		return when {
			value is PGobject -> value.value!!
			value is String -> value
			else -> error("Unexpected value $value of type ${value::class.qualifiedName}")
		}
	}

	override fun readObject(rs: ResultSet, index: Int): Any? {
		return rs.getString(index)
	}

	override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
		val obj = PGobject()
		obj.type = "jsonb"
		obj.value = value as String?
		stmt[index] = obj
	}
}

fun Table.jsonb(name: String): Column<String> = registerColumn(name, JsonBinary())