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
    compile(files("../libs/DreamCore-shadow.jar"))
    compile("net.perfectdreams.dreamcorreios:DreamCorreios:1.0-SNAPSHOT")
    compile("net.perfectdreams.dreamcasamentos:DreamCasamentos:1.0-SNAPSHOT")
    compile(project(":DreamClubes"))
    compileOnly(files("../libs/ProtocolSupport.jar"))
    compileOnly(files("../libs/mcMMO.jar"))
    compile("net.milkbowl.vault:VaultAPI:1.6")
    compile("com.greatmancode:craftconomy3:3.3.1")
    compile(project(":DreamVote"))
    compile(project(":DreamVanish"))
    compile("net.luckperms:api:5.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
