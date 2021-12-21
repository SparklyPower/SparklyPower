package net.perfectdreams.dreamterrainadditions

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.scheduler.BukkitRunnable

class ClaimTrustExpirationTask(val m: DreamTerrainAdditions) : BukkitRunnable() {
    override fun run() {
        var isDirty = false

        runBlocking {
            m.claimsAdditionsList.forEach { additions ->
                additions.temporaryTrustedPlayersMutex.withLock {
                    additions.temporaryTrustedPlayers.forEach { (uuid, millis) ->
                        val griefPreventionClaim = GriefPrevention.instance.dataStore.getClaim(additions.claimId)

                        if (System.currentTimeMillis() >= millis) {
                            additions.temporaryTrustedPlayers.remove(uuid)

                            griefPreventionClaim.dropPermission(uuid.toString())
                            GriefPrevention.instance.dataStore.saveClaim(griefPreventionClaim)
                            isDirty = true
                        }
                    }
                }
            }
        }

        // Save additions.json file if any claim had a change
        if (isDirty)
            m.saveInAsyncTask()
    }
}