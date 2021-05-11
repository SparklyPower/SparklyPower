package net.perfectdreams.dreamcore.utils.codecs

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bukkit.Bukkit
import org.bukkit.Location

class LocationCodec : Codec<Location> {
	override fun getEncoderClass(): Class<Location> {
		return Location::class.java
	}

	override fun encode(writer: BsonWriter, p1: Location?, p2: EncoderContext) {
		if (p1 == null)
			return
		writer.writeStartDocument()
		writer.writeName("x")
		writer.writeDouble(p1.x)
		writer.writeName("y")
		writer.writeDouble(p1.y)
		writer.writeName("z")
		writer.writeDouble(p1.z)
		writer.writeName("yaw")
		writer.writeDouble(p1.yaw.toDouble())
		writer.writeName("pitch")
		writer.writeDouble(p1.pitch.toDouble())
		writer.writeName("world")
		writer.writeString(p1.world.name)
		writer.writeEndDocument()
	}

	override fun decode(reader: BsonReader, p1: DecoderContext): Location {
		reader.readStartDocument()
		val x = reader.readDouble("x")
		val y = reader.readDouble("y")
		val z = reader.readDouble("z")
		val yaw = reader.readDouble("yaw").toFloat()
		val pitch = reader.readDouble("pitch").toFloat()
		val world = reader.readString("world")
		reader.readEndDocument()

		return Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
	}
}