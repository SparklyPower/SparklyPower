import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    `java-library`
    id("com.github.johnrengelman.shadow") version "4.0.4"
    kotlin("plugin.serialization") version "1.4.32"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
}

// This is our DreamCore configuration that has all the dependencies (so plugins can just need to include the project as a project dependency)
//
// The reason we needed to do this is because we want ALL the declared "api" dependencies here BUT we also want the relocated dependencies
// done by the shadow JAR!
//
// So, to do that, we create a configuration that extends the default "shadow" configuration and the "compileClasspath" configuration, this allows us
// to satisfy both of our needs!
val shadowWithRuntimeDependencies by configurations.creating {
    // If you want this configuration to share the same dependencies, otherwise omit this line
    extendsFrom(configurations["shadow"], configurations["compileClasspath"])
}

dependencies {
    compileOnlyApi("io.github.waterfallmc:waterfall-proxy:1.16-R0.4-SNAPSHOT")

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    api("com.zaxxer:HikariCP:4.0.3")
    api("org.postgresql:postgresql:42.2.20")
    api("org.jetbrains.exposed:exposed-core:0.32.1")
    api("org.jetbrains.exposed:exposed-dao:0.32.1")
    api("org.jetbrains.exposed:exposed-jdbc:0.32.1")
    api("io.ktor:ktor-client-cio:1.5.4")

    api(Dependencies.DISCORD_WEBHOOKS)

    api("com.github.salomonbrys.kotson:kotson:2.5.0")

    api("org.mongodb:mongo-java-driver:3.7.0-rc0")
    api("com.github.kevinsawicki:http-request:6.0")

    api("net.perfectdreams.commands:command-framework-core:0.0.8")
    api("net.perfectdreams.minecraftmojangapi:minecraft-mojang-api:0.0.1-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("DreamCoreBungee-shadow")

        relocate("org.mongodb", "net.perfectdreams.libs.org.mongodb")
        relocate("com.mongodb", "net.perfectdreams.libs.com.mongodb")
        relocate("org.bson", "net.perfectdreams.libs.org.bson")

        exclude {
            val file = it.file
            (file?.name?.startsWith("kotlin") == true && file?.name?.contains("serialization") == false) || file?.name?.startsWith("patched_") == true
        }
    }

    "build" {
        dependsOn(shadowJar)
    }
}