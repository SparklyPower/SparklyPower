import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.citizensnpcs.co/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("../libs/patched_1.15.1.jar"))
    implementation(files("../libs/DreamCore-shadow.jar"))
    implementation(files("../libs/WorldGuard.jar"))
    implementation("net.citizensnpcs:citizens:2.0.26-SNAPSHOT")
    implementation("net.luckperms:api:5.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
