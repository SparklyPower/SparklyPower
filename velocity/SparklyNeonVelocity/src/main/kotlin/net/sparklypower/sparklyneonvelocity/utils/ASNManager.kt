package net.sparklypower.sparklyneonvelocity.utils

import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.tables.BlockedASNs
import org.apache.commons.net.util.SubnetUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ASNManager(val m: SparklyNeonVelocity) {
    val asns = mutableMapOf<Int, ASN>()
    // To speed up asn checking, we are going to cache all the "ASN hits" in set
    // Then, when the getAsnForIp() method is called, we are going to check FIRST all the already hit ASNs
    // This way we can avoid a lot of unnecessary ASN checks!
    val triggeredAsns = Collections.newSetFromMap(ConcurrentHashMap<Int, Boolean>())

    fun load() {
        val file = File(m.dataFolder, "GeoLite2-ASN-Blocks-IPv4.csv")

        var idx = 0

        file.bufferedReader().useLines {
            for (line in it) {
                if (idx != 0) { // != 0, header
                    val split = line.split(",")

                    val ip = split[0].removePrefix("\"")
                        .removeSuffix("\"")

                    val asnNumber = split[1].removePrefix("\"")
                        .removeSuffix("\"")
                        .toInt()

                    val asnName = split[2].removePrefix("\"")
                        .removeSuffix("\"")

                    val asn = asns.getOrPut(asnNumber, { ASN(asnName) })
                    val ipAndRange = ip.split("/")
                    val ipSplit = ipAndRange.first().split(".")

                    asn.ranges.add(
                        IPWithBitmask(
                            ipSplit[0].toInt(),
                            ipSplit[1].toInt(),
                            ipSplit[2].toInt(),
                            ipSplit[3].toInt(),
                            ipAndRange.last().toInt()
                        )
                    )
                }
                idx++
            }
        }
    }

    suspend fun isAsnBlacklisted(source: String): Boolean {
        val asn = getAsnForIP(source) ?: return false

        return m.pudding.transaction {
            BlockedASNs.select {
                BlockedASNs.id eq asn.first
            }.count() != 0L
        }
    }

    fun getAsnForIP(source: String): Pair<Int, ASN>? {
        if (source.contains(":")) // whoops, no IPv6 support yet!
            return null

        if (source == "127.0.0.1") // Localhost will never be non null
            return null

        val split = source.split(".")

        val first = split[0].toInt()
        val second = split[1].toInt()
        val third = split[2].toInt()
        val fourth = split[3].toInt()

        // Fast Check: Check triggered ASNs first, then check non triggered ASNs
        for ((asnNumber, asnInfo) in asns.entries.sortedByDescending { it.key in triggeredAsns }) {
            val isThisAsn = asnInfo.isThisAsn(first, second, third, fourth)

            if (isThisAsn) {
                // Store a triggered ASN check in the triggeredAsnsCount set
                triggeredAsns.add(asnNumber)
                return Pair(asnNumber, asnInfo)
            }
        }

        return null
    }

    data class ASN(
        val name: String,
        val x: String? = null
    ) {
        val ranges = mutableListOf<IPWithBitmask>()

        fun isThisAsn(source: String): Boolean {
            val split = source.split(".")

            val first = split[0].toInt()
            val second = split[1].toInt()
            val third = split[2].toInt()
            val fourth = split[3].toInt()

            return isThisAsn(first, second, third, fourth)
        }

        fun isThisAsn(first: Int, second: Int, third: Int, fourth: Int): Boolean {
            for (range in ranges) {
                var matchedRange: IPWithBitmask? = range

                if (range.mask in 24..32) {
                    if (range.first == first && range.second == second && range.third == third) {
                        matchedRange = range
                    }
                } else if (range.mask in 16..23) {
                    if (range.first == first && range.second == second) {
                        matchedRange = range
                    }
                } else {
                    if (range.first == first) {
                        matchedRange = range
                    }
                }

                if (matchedRange != null) {
                    val subnetUtils = SubnetUtils("${matchedRange.first}.${matchedRange.second}.${matchedRange.third}.${matchedRange.fourth}/${matchedRange.mask}")

                    if (subnetUtils.getInfo().isInRange("$first.$second.$third.$fourth"))
                        return true
                }
            }

            return false
        }
    }

    data class IPWithBitmask(
        val first: Int,
        val second: Int,
        val third: Int,
        val fourth: Int,
        val mask: Int
    )
}