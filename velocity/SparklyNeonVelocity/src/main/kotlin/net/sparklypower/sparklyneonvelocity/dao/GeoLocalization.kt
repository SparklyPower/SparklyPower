package net.sparklypower.sparklyneonvelocity.dao

import net.sparklypower.sparklyneonvelocity.tables.GeoLocalizations
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GeoLocalization(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<GeoLocalization>(GeoLocalizations)

	var ip by GeoLocalizations.ip
	
	var region by GeoLocalizations.region
	var country by GeoLocalizations.country
	var retrievedAt by GeoLocalizations.retrievedAt
}