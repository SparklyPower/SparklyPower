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
    implementation(files("../libs/paper_server.jar"))
    implementation(files("../libs/DreamCore-shadow.jar"))
    implementation("net.citizensnpcs:citizens:2.0.26-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
