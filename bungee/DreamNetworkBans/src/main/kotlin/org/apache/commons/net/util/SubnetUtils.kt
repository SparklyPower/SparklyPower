package org.apache.commons.net.util

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A class that performs some subnet calculations given a network address and a subnet mask.
 * @see "http://www.faqs.org/rfcs/rfc1519.html"
 *
 * @since 2.0
 */
class SubnetUtils {
    private val netmask: Int
    private val address: Int
    private val network: Int
    private val broadcast: Int
    /** Whether the broadcast/network address are included in host count  */
    private var inclusiveHostCount = false

    /**
     * Constructor that takes a CIDR-notation string, e.g. "192.168.0.1/16"
     * @param cidrNotation A CIDR-notation string, e.g. "192.168.0.1/16"
     * @throws IllegalArgumentException if the parameter is invalid,
     * i.e. does not match n.n.n.n/m where n=1-3 decimal digits, m = 1-2 decimal digits in range 0-32
     */
    constructor(cidrNotation: String) {
        val matcher = cidrPattern.matcher(cidrNotation)
        if (matcher.matches()) {
            address = matchAddress(matcher)
            /* Create a binary netmask from the number of bits specification /x */
            val trailingZeroes = NBITS - rangeCheck(matcher.group(5).toInt(), 0, NBITS)
            /*
           * An IPv4 netmask consists of 32 bits, a contiguous sequence
           * of the specified number of ones followed by all zeros.
           * So, it can be obtained by shifting an unsigned integer (32 bits) to the left by
           * the number of trailing zeros which is (32 - the # bits specification).
           * Note that there is no unsigned left shift operator, so we have to use
           * a long to ensure that the left-most bit is shifted out correctly.
           */netmask = (0x0FFFFFFFFL shl trailingZeroes).toInt()
            /* Calculate base network address */network = address and netmask
            /* Calculate broadcast address */broadcast = network or netmask.inv()
        } else {
            throw IllegalArgumentException("Could not parse [$cidrNotation]")
        }
    }

    /**
     * Constructor that takes a dotted decimal address and a dotted decimal mask.
     * @param address An IP address, e.g. "192.168.0.1"
     * @param mask A dotted decimal netmask e.g. "255.255.0.0"
     * @throws IllegalArgumentException if the address or mask is invalid,
     * i.e. does not match n.n.n.n where n=1-3 decimal digits and the mask is not all zeros
     */
    constructor(address: String, mask: String) {
        this.address = toInteger(address)
        netmask = toInteger(mask)
        require((netmask and -netmask) - 1 == netmask.inv()) { "Could not parse [$mask]" }
        /* Calculate base network address */network = this.address and netmask
        /* Calculate broadcast address */broadcast = network or netmask.inv()
    }

    /**
     * Returns `true` if the return value of [SubnetInfo.getAddressCount]
     * includes the network and broadcast addresses.
     * @since 2.2
     * @return true if the host count includes the network and broadcast addresses
     */
    fun isInclusiveHostCount(): Boolean {
        return inclusiveHostCount
    }

    /**
     * Set to `true` if you want the return value of [SubnetInfo.getAddressCount]
     * to include the network and broadcast addresses.
     * @param inclusiveHostCount true if network and broadcast addresses are to be included
     * @since 2.2
     */
    fun setInclusiveHostCount(inclusiveHostCount: Boolean) {
        this.inclusiveHostCount = inclusiveHostCount
    }

    /**
     * Convenience container for subnet summary information.
     *
     */
    inner class SubnetInfo {
        // long versions of the values (as unsigned int) which are more suitable for range checking
        private fun networkLong(): Long {
            return (network and UNSIGNED_INT_MASK.toInt()).toLong()
        }

        private fun broadcastLong(): Long {
            return (broadcast and UNSIGNED_INT_MASK.toInt()).toLong()
        }

        private fun low(): Int {
            return if (isInclusiveHostCount()) network else if (broadcastLong() - networkLong() > 1) network + 1 else 0
        }

        private fun high(): Int {
            return if (isInclusiveHostCount()) broadcast else if (broadcastLong() - networkLong() > 1) broadcast - 1 else 0
        }

        /**
         * Returns true if the parameter `address` is in the
         * range of usable endpoint addresses for this subnet. This excludes the
         * network and broadcast addresses.
         * @param address A dot-delimited IPv4 address, e.g. "192.168.0.1"
         * @return True if in range, false otherwise
         */
        fun isInRange(address: String): Boolean {
            return isInRange(toInteger(address))
        }

        /**
         * Returns true if the parameter `address` is in the
         * range of usable endpoint addresses for this subnet. This excludes the
         * network and broadcast addresses.
         * @param address the address to check
         * @return true if it is in range
         * @since 3.4 (made public)
         */
        fun isInRange(address: Int): Boolean {
            if (address == 0) { // cannot ever be in range; rejecting now avoids problems with CIDR/31,32
                return false
            }
            val addLong = (address and UNSIGNED_INT_MASK.toInt()).toLong()
            val lowLong = (low() and UNSIGNED_INT_MASK.toInt()).toLong()
            val highLong = (high() and UNSIGNED_INT_MASK.toInt()).toLong()
            return addLong >= lowLong && addLong <= highLong
        }

        val broadcastAddress: String
            get() = format(toArray(broadcast))

        val networkAddress: String
            get() = format(toArray(network))

        fun getNetmask(): String {
            return format(toArray(netmask))
        }

        fun getAddress(): String {
            return format(toArray(address))
        }

        /**
         * Return the low address as a dotted IP address.
         * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
         *
         * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
         */
        val lowAddress: String
            get() = format(toArray(low()))

        /**
         * Return the high address as a dotted IP address.
         * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
         *
         * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
         */
        val highAddress: String
            get() = format(toArray(high()))

        /**
         * Get the count of available addresses.
         * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
         * @return the count of addresses, may be zero.
         * @since 3.4
         */
        fun getAddressCountLong(): Long {
            val b = broadcastLong()
            val n = networkLong()
            val count = b - n + if (isInclusiveHostCount()) 1 else -1
            return if (count < 0) 0 else count
        }

        fun asInteger(address: String): Int {
            return toInteger(address)
        }

        fun getCidrSignature(): String {
            return format(toArray(address)) + "/" + pop(netmask)
        }

        fun getAllAddresses(): List<String> {
            val ct = getAddressCountLong()
            val addresses = arrayOfNulls<String>(ct.toInt())
            if (ct == 0L) {
                return addresses.filterNotNull()
            }
            var add = low()
            var j = 0
            while (add <= high()) {
                addresses[j] = format(toArray(add))
                ++add
                ++j
            }
            return addresses.filterNotNull()
        }

        /**
         * {@inheritDoc}
         * @since 2.2
         */
        override fun toString(): String {
            val buf = StringBuilder()
            buf.append("CIDR Signature:\t[").append(getCidrSignature()).append("]")
                    .append(" Netmask: [").append(getNetmask()).append("]\n")
                    .append("Network:\t[").append(networkAddress).append("]\n")
                    .append("Broadcast:\t[").append(broadcastAddress).append("]\n")
                    .append("First Address:\t[").append(lowAddress).append("]\n")
                    .append("Last Address:\t[").append(highAddress).append("]\n")
                    .append("# Addresses:\t[").append(getAddressCountLong()).append("]\n")
            return buf.toString()
        }
    }

    /**
     * Return a [SubnetInfo] instance that contains subnet-specific statistics
     * @return new instance
     */
    fun getInfo(): SubnetInfo {
        return SubnetInfo()
    }

    /*
     * Convert a packed integer address into a 4-element array
     */
    private fun toArray(`val`: Int): IntArray {
        val ret = IntArray(4)
        for (j in 3 downTo 0) {
            ret[j] = ret[j] or (`val` ushr 8 * (3 - j) and 0xff)
        }
        return ret
    }

    /*
     * Convert a 4-element array into dotted decimal format
     */
    private fun format(octets: IntArray): String {
        val str = StringBuilder()
        for (i in octets.indices) {
            str.append(octets[i])
            if (i != octets.size - 1) {
                str.append(".")
            }
        }
        return str.toString()
    }

    /*
     * Count the number of 1-bits in a 32-bit integer using a divide-and-conquer strategy
     * see Hacker's Delight section 5.1
     */
    fun pop(x: Int): Int {
        var x = x
        x = x - (x ushr 1 and 0x55555555)
        x = (x and 0x33333333) + (x ushr 2 and 0x33333333)
        x = x + (x ushr 4) and 0x0F0F0F0F
        x = x + (x ushr 8)
        x = x + (x ushr 16)
        return x and 0x0000003F
    }

    companion object {
        private const val UNSIGNED_INT_MASK = 0x0FFFFFFFFL
        private const val IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})"
        private const val SLASH_FORMAT = "$IP_ADDRESS/(\\d{1,2})" // 0 -> 32
        private val addressPattern = Pattern.compile(IP_ADDRESS)
        private val cidrPattern = Pattern.compile(SLASH_FORMAT)
        private const val NBITS = 32
        /*
     * Convert a dotted decimal format address to a packed integer format
     */
        private fun toInteger(address: String): Int {
            val matcher = addressPattern.matcher(address)
            return if (matcher.matches()) {
                matchAddress(matcher)
            } else {
                throw IllegalArgumentException("Could not parse [$address]")
            }
        }

        /*
     * Convenience method to extract the components of a dotted decimal address and
     * pack into an integer using a regex match
     */
        private fun matchAddress(matcher: Matcher): Int {
            var addr = 0
            for (i in 1..4) {
                val n = rangeCheck(matcher.group(i).toInt(), 0, 255)
                addr = addr or (n and 0xff shl 8 * (4 - i))
            }
            return addr
        }

        /*
     * Convenience function to check integer boundaries.
     * Checks if a value x is in the range [begin,end].
     * Returns x if it is in range, throws an exception otherwise.
     */
        private fun rangeCheck(value: Int, begin: Int, end: Int): Int {
            if (value >= begin && value <= end) { // (begin,end]
                return value
            }
            throw IllegalArgumentException("Value [$value] not in range [$begin,$end]")
        }
    }
}