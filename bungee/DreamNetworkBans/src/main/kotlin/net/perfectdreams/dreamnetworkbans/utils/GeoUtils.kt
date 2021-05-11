package net.perfectdreams.dreamnetworkbans.utils

import com.github.kevinsawicki.http.HttpRequest
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.dreamcorebungee.utils.DreamUtils

object GeoUtils {
	
	fun getGeolocalization(player: ProxiedPlayer) = getGeolocalization(player.address.hostString)
	
	fun getGeolocalization(address: String): GeoLocalization {
		val request = HttpRequest.get("http://ip-api.com/json/$address")
				.connectTimeout(2500)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0")

		if (!request.ok())
			throw RuntimeException("Request for http://ip-api.com/json/$address is not OK")
		
		return DreamUtils.gson.fromJson(request.body(), GeoLocalization::class.java)
	}
	
	class GeoLocalization(
			val `as`: String = "???",
			val city: String = "???",
			val country: String = "???",
			val countryCode: String = "???",
			val isp: String = "???",
			val lat: String = "???",
			val lon: String = "???",
			val org: String = "???",
			val query: String = "???",
			val region: String = "???",
			val regionName: String = "???",
			val status: String = "???",
			val timezone: String = "???",
			val zip: String = "???"
	)
}