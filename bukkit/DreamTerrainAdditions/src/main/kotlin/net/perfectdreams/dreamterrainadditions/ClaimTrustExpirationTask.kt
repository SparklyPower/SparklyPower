package net.perfectdreams.dreamterrainadditions

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import me.ryanhamshire.GriefPrevention.Claim
import me.ryanhamshire.GriefPrevention.GriefPrevention
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class ClaimTrustExpirationTask(val m: DreamTerrainAdditions) : BukkitRunnable() {
    override fun run() {
        var isDirty = false

        runBlocking {
            m.claimsAdditionsMap.values.forEach { additions ->
                additions.temporaryTrustedPlayersMutex.withLock {
                    additions.temporaryTrustedPlayers.forEach { (uuid, millis) ->
                        val claimResult = getClaimOrSubclaimById(additions.claimId)

                        if (System.currentTimeMillis() >= millis) {
                            m.logger.info { "Checking if $uuid's permission must be cleaned up in claim ${additions.claimId} (${claimResult})" }

                            additions.temporaryTrustedPlayers.remove(uuid)

                            if (claimResult != null) {
                                claimResult.claim.dropPermission(uuid.toString())
                                GriefPrevention.instance.dataStore.saveClaim(claimResult.claim)
                            }

                            isDirty = true

                            // Is the owner online?
                            val ownerId = (claimResult?.parentClaim ?: claimResult?.claim)?.ownerID
                            if (ownerId != null)
                                Bukkit.getPlayer(ownerId)?.sendMessage("§aA permissão do player $uuid foi removida do terreno ${additions.claimId} pois a permissão expirou")
                        }
                    }
                }
            }
        }

        // Save additions.json file if any claim had a change
        if (isDirty)
            m.saveInAsyncTask()
    }

    fun getClaimOrSubclaimById(claimId: Long): ClaimResult? {
        // While getClaim is cool, we aren't able to check subclaims with it
        for (claim in GriefPrevention.instance.dataStore.claims) {
            if (claim.inDataStore) {
                if (claim.id == claimId)
                    return ClaimResult(
                        claim,
                        null
                    )

                for (childClaim in claim.children) {
                    if (childClaim.id == claimId)
                        return ClaimResult(
                            childClaim,
                            claim
                        )
                }
            }
        }
        return null
    }

    data class ClaimResult(
        val claim: Claim,
        val parentClaim: Claim?
    )
}