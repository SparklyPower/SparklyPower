package net.perfectdreams.dreamcore.utils

import org.bukkit.Location
import java.util.HashSet


object GeometryUtils {
    fun makeHollow(blocks: Set<Location>, sphere: Boolean): Set<Location> {
        val edge = HashSet<Location>()
        if (!sphere) {
            for (l in blocks) {
                val w = l.world
                val X = l.blockX
                val Y = l.blockY
                val Z = l.blockZ
                val front = Location(w, (X + 1).toDouble(), Y.toDouble(), Z.toDouble())
                val back = Location(w, (X - 1).toDouble(), Y.toDouble(), Z.toDouble())
                val left = Location(w, X.toDouble(), Y.toDouble(), (Z + 1).toDouble())
                val right = Location(w, X.toDouble(), Y.toDouble(), (Z - 1).toDouble())
                if (!blocks.contains(front) || !blocks.contains(back) || !blocks.contains(left) || !blocks.contains(right)) {
                    edge.add(l)
                }
            }
            return edge
        }
        for (l in blocks) {
            val w = l.world
            val X = l.blockX
            val Y = l.blockY
            val Z = l.blockZ
            val front = Location(w, (X + 1).toDouble(), Y.toDouble(), Z.toDouble())
            val back = Location(w, (X - 1).toDouble(), Y.toDouble(), Z.toDouble())
            val left = Location(w, X.toDouble(), Y.toDouble(), (Z + 1).toDouble())
            val right = Location(w, X.toDouble(), Y.toDouble(), (Z - 1).toDouble())
            val top = Location(w, X.toDouble(), (Y + 1).toDouble(), Z.toDouble())
            val bottom = Location(w, X.toDouble(), (Y - 1).toDouble(), Z.toDouble())
            if (!blocks.contains(front) || !blocks.contains(back) || !blocks.contains(left) || !blocks.contains(right) || !blocks.contains(top) || !blocks.contains(bottom)) {
                edge.add(l)
            }
        }
        return edge
    }

    fun circle(pos: Location, radius: Int, hollow: Boolean): Set<Location> {
        val blocks = HashSet<Location>()
        val X = pos.blockX
        val Y = pos.blockY
        val Z = pos.blockZ
        val radiusSquared = radius * radius
        val w = pos.world
        if (hollow) {
            for (x in X - radius..X + radius) {
                for (z in Z - radius..Z + radius) {
                    if ((X - x) * (X - x) + (Z - z) * (Z - z) <= radiusSquared) {
                        val block = Location(w, x.toDouble(), Y.toDouble(), z.toDouble())
                        blocks.add(block)
                    }
                }
            }
            return makeHollow(blocks, false)
        }
        for (x in X - radius..X + radius) {
            for (z in Z - radius..Z + radius) {
                if ((X - x) * (X - x) + (Z - z) * (Z - z) <= radiusSquared) {
                    val block = Location(w, x.toDouble(), Y.toDouble(), z.toDouble())
                    blocks.add(block)
                }
            }
        }
        return blocks
    }

    fun sphere(Location: Location, radius: Int, hollow: Boolean): Set<Location> {
        val blocks = HashSet<Location>()
        val w = Location.world
        val X = Location.blockX
        val Y = Location.blockY
        val Z = Location.blockZ
        val radiusSquared = radius * radius
        if (hollow) {
            for (x in X - radius..X + radius) {
                for (y in Y - radius..Y + radius) {
                    for (z in Z - radius..Z + radius) {
                        if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared) {
                            val block = Location(w, x.toDouble(), y.toDouble(), z.toDouble())
                            blocks.add(block)
                        }
                    }
                }
            }
            return makeHollow(blocks, true)
        }
        for (x in X - radius..X + radius) {
            for (y in Y - radius..Y + radius) {
                for (z in Z - radius..Z + radius) {
                    if ((X - x) * (X - x) + (Y - y) * (Y - y) + (Z - z) * (Z - z) <= radiusSquared) {
                        val block = Location(w, x.toDouble(), y.toDouble(), z.toDouble())
                        blocks.add(block)
                    }
                }
            }
        }
        return blocks
    }
}