pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin2js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
        eachPlugin {
            if (requested.id.id == "kotlin-multiplatform") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}

rootProject.name = "sparklypower-parent"

include(":DreamCore")
include(":DreamAuth")
include(":DreamCash")
include(":DreamVote")
include(":DreamQuiz")
include(":DreamCorrida")
include(":DreamChat")
include(":DreamChallenges")
include(":DreamMoverSpawners")
include(":DreamLoja")
include(":DreamCaixaSecreta")
include(":DreamJetpack")
include(":DreamMochilas")
include(":DreamEnchant")
include(":DreamTrails")
include(":DreamRaspadinha")
include(":DreamHome")
include(":DreamMini")
include(":DreamResourcePack")
include(":DreamCasamentos")
include(":DreamClubes")
include(":DreamScoreboard")
include(":DreamTorreDaMorte")
include(":DreamRestarter")
include(":DreamVanish")
include(":DreamLobbyFun")
include(":DreamMinaRecheada")
include(":DreamBusca")
include(":DreamAssinaturas")
include(":DreamBlockVIPItems")