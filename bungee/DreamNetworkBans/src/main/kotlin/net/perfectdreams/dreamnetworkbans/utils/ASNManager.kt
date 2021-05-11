package net.perfectdreams.dreamnetworkbans.utils

import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import org.apache.commons.net.util.SubnetUtils
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ASNManager(val m: DreamNetworkBans) {
    companion object {
        private val BLACKLISTED_ASNS = listOf(
                14061, // DIGITALOCEAN-ASN
                23470, // RELIABLESITE
                9009, // M247 Ltd
                16247, // M247 Ltd
                47447, // 23media GmbH
                39572, // DataWeb Global Group B.V.
                34920, // Simply Transit Ltd
                29550, // Simply Transit Ltd
                36351, // SOFTLAYER
                39020, // Comvive Servidores S.L.
                16276, // OVH SAS
                35540, // OVH SAS
                42926, // Radore Veri Merkezi Hizmetleri A.S.
                8100, // ASN-QUADRANET-GLOBAL
                63949, // Linode
                48337, // Linode
                60558, // Phoenix Nap
                57872, // Phoenix Nap
                29656, // Phoenix Nap
                207134, // Phoenix Nap
                50389, // Phoenix Nap
                39239, // Phoenix Nap
                210266, // Phoenix Nap
                55081, // 24SHELLS
                20454, // SSASN2
                19437, // SS-ASH
                205399, // Hostigger INC.
                13213, // UK-2 Limited
                209181, // Zenex 5ive Limited
                51765, // Oy Crea Nova Hosting Solution Ltd
                60781, // LeaseWeb Netherlands B.V.
                28753, // Leaseweb Deutschland GmbH
                205544, // Leaseweb Uk Limited
                395954, // LEASEWEB-USA-LAX-11
                396190, // LEASEWEB-USA-SEA-10
                7203, // LEASEWEB-USA-SFO-12
                393886, // LEASEWEB-USA-MIA-11
                19148, // LEASEWEB-USA-PHX-11
                396362, // LEASEWEB-USA-NYC-11
                134351, // Leaseweb Japan K.K.
                30633, // LEASEWEB-USA-WDC
                394380, // LEASEWEB-USA-DAL-10
                59253, // Leaseweb Asia Pacific pte. ltd.
                133752, // Leaseweb Asia Pacific pte. ltd.
                38930, // LeaseWeb Network B.V.
                60626, // LeaseWeb CDN B.V.
                136988, // LEASEWEB AUSTRALIA PTY LIMITED
                20473, // AS-CHOOPA
                20860, // Iomart Cloud Services Limited
                21130, // Iomart Cloud Services Limited
                42831, // UK Dedicated Servers Limited
                58940, // Dedicated Servers Australia
                51783, // The Center of Dedicated Servers LLC
                24238, // Dedicated Servers - Brisbane
                29854, // WESTHOST
                49981, // WorldStream B.V.
                205016, // HERN Labs AB
                60068, // Datacamp Limited
                212238, // Datacamp Limited
                40676, // AS40676
                63018, // DEDICATED
                32780, // HOSTINGSERVICES-INC
                21100, // ITL LLC
                15626, // ITL LLC
                50979, // ITL LLC
                59729, // ITL LLC
                55286, // SERVER-MANIA
                56630, // Melbikomas UAB
                49287, // Melbikomas UAB
                51453, // ANEXIA Internetdienstleistungs GmbH
                42473, // ANEXIA Internetdienstleistungs GmbH
                42388, // ANEXIA Internetdienstleistungs GmbH
                42354, // ANEXIA Internetdienstleistungs GmbH
                47147, // ANEXIA Internetdienstleistungs GmbH
                40980, // ANEXIA Internetdienstleistungs GmbH
                199159, // ANEXIA Internetdienstleistungs GmbH
                56478, // Hyperoptic Ltd
                9123, // TimeWeb Ltd.
                27589, // MOJOHOST
                42567, // Mojohost B.v.
                24940, // Hetzner Online GmbH
                396319, // CLOUDVPN-AS
                202448, // MVPS LTD
                200487, // OOO VPS
                398271, // HVPN
                62468, // VPSQUAN
                397384, // LAUNCHVPS
                267826, // VPS GURU CHILE SPA
                46868, // VPNET-2
                3644, // SPR-VPN
                40819, // VPSDATACENTER
                20448, // VPNTRANET-LLC
                210119, // VPSSC Networks LTD
                50578, // SteamVPS SRL
                135756, // VPS Broadband And Telecommunications Pvt Ltd
                135952, // VPS Securities Joint Stock Company
                6188, // VPSDATACENTER
                29028, // DirectVPS B.V.
                198485, // DirectVPS B.V.
                209103, // ProtonVPN AG
                204725, // UA VPS LLC
                61493, // InterBS S.R.L. (BAEHOST)
                26464, // JOYENT-INC
                49367, // Seflow S.N.C. Di Marco Brame' & C.
                31122, // Digiweb ltd
                53667, // PONYNET
                57169, // EDIS GmbH
                201924, // ENAHOST s.r.o.
                63023, // AS-GLOBALTELEHOST
                62563, // AS-GLOBALTELEHOST
                29119, // ServiHosting Networks S.L.
                199081, // LANCOM LTD
                60911, // LANCOM LTD
                207034, // LANCOM LTD
                212228, // servinga GmbH
                207408, // servinga GmbH
                39378, // servinga GmbH
                59711, // HZ Hosting Ltd
                61046, // HZ Hosting Ltd
                201525, // HZ Hosting Ltd
                202015, // HZ Hosting Ltd
                396356, // MAXIHOST
                12989, // StackPath LLC
                199156, // StackPath LLC
                42160 // lcp nv
        )
    }

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

    fun isAsnBlacklisted(source: String): Boolean {
        val asn = getAsnForIP(source) ?: return false

        return asn.first in BLACKLISTED_ASNS
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