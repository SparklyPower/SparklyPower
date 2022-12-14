import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    `java-library`
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("plugin.serialization")
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
    api(project(":common:KotlinRuntime"))
    compileOnlyApi("io.github.waterfallmc:waterfall-proxy:1.16-R0.4-SNAPSHOT")

    api(project(":common:tables"))
    api(project(":common:rpc-payloads"))

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

    api(Dependencies.DISCORD_WEBHOOKS)

    api("com.github.salomonbrys.kotson:kotson:2.5.0")

    api("com.github.kevinsawicki:http-request:6.0")

    api("io.ktor:ktor-client-cio:2.1.3")
    api("io.ktor:ktor-server-netty:2.1.3")

    api("net.perfectdreams.commands:command-framework-core:0.0.8")
    api("net.perfectdreams.minecraftmojangapi:minecraft-mojang-api:0.0.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("DreamCoreBungee-shadow")

        exclude {
            it?.file?.name?.startsWith("patched_") == true
        }
    }

    "build" {
        dependsOn(shadowJar)
    }
}