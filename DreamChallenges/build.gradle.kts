import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(project(":DreamCash"))
    compile("com.destroystokyo.paper:paper:1.13-R0.1-SNAPSHOT")
    compile("net.perfectdreams.dreamcore:DreamCore:1.0-SNAPSHOT")
    compile("net.perfectdreams.dreamcorreios:DreamCorreios:1.0-SNAPSHOT")
    compile("net.perfectdreams.dreamchat:DreamChat:1.0-SNAPSHOT")
    compile("net.milkbowl.vault:VaultAPI:1.6")
    compile("com.vexsoftware:nuvotifier-bukkit:2.5.1-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
