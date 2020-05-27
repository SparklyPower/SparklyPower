import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compileOnly("com.comphenix.protocol:ProtocolLib:4.4.0-SNAPSHOT")
    compile(files("../libs/patched_1.15.1.jar"))
    compile(files("../libs/DreamCore-shadow.jar"))
    compile(files("../libs/WorldGuard.jar"))
    compileOnly("com.github.TechFortress:GriefPrevention:16.11.5")
    compile(files("../libs/ChestShop.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
