import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    `java-library`
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
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
    compileOnlyApi(project(":common:KotlinRuntime"))
    compileOnlyApi(files("../../libs/paper_server.jar"))
    // compileOnly(files("../../libs/ProtocolSupport.jar"))
    compileOnlyApi("com.comphenix.protocol:ProtocolLib:4.6.0")
    compileOnlyApi(files("../../libs/WorldEdit.jar"))
    compileOnlyApi(files("../../libs/WorldGuard.jar"))
    compileOnlyApi("com.github.TechFortress:GriefPrevention:16.17.1")
    api("net.perfectdreams.commands:command-framework-core:0.0.8")
    api("com.github.SparklyPower:PacketWrapper:88ddd591d8")
    compileOnlyApi("net.milkbowl.vault:VaultAPI:1.6")
    compileOnlyApi("com.github.MascusJeoraly:LanguageUtils:1.9")
    api("com.google.code.gson:gson:2.8.7")
    api("org.mongodb:mongo-java-driver:3.7.0-rc0")
    api("com.zaxxer:HikariCP:2.7.8")
    api("org.postgresql:postgresql:42.2.5")
    api("org.xerial:sqlite-jdbc:3.30.1")
    api("org.jetbrains.exposed:exposed-core:0.32.1")
    api("org.jetbrains.exposed:exposed-dao:0.32.1")
    api("org.jetbrains.exposed:exposed-jdbc:0.32.1")
    api("com.okkero.skedule:skedule:1.2.4.1-SNAPSHOT")
    api("com.github.kevinsawicki:http-request:6.0")
    api("org.ow2.asm:asm:7.0")
    api("org.ow2.asm:asm-util:7.0")
    api("commons-codec:commons-codec:1.12")
    api("org.apache.commons:commons-lang3:3.9")
    api("org.mindrot:jbcrypt:0.4")
    api("com.github.salomonbrys.kotson:kotson:2.5.0")
    api(Dependencies.DISCORD_WEBHOOKS)
    api("com.github.ben-manes.caffeine:caffeine:2.6.2")
    api("org.apache.commons:commons-text:1.8")
    compileOnlyApi("com.greatmancode:craftconomy3:3.3.1")
    compileOnlyApi("me.lucko.luckperms:luckperms-api:4.3")
    testCompileOnly(files("../../libs/paper_server.jar"))
    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.3.0-M1")
    testCompileOnly("org.junit.jupiter:junit-jupiter-engine:5.3.0-M1")
    testCompileOnly("io.mockk:mockk:1.9")
    testCompileOnly("org.assertj:assertj-core:3.10.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("DreamCore-shadow")

        relocate("org.mongodb", "net.perfectdreams.libs.org.mongodb")
        relocate("com.mongodb", "net.perfectdreams.libs.com.mongodb")
        relocate("org.bson", "net.perfectdreams.libs.org.bson")

        exclude {
            it.file?.name?.startsWith("kotlin") == true || it.file?.name?.startsWith("patched_") == true
        }
    }

    "build" {
        dependsOn(shadowJar)
    }
}