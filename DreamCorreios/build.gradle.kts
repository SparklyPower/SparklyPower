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
    compile(kotlin("stdlib-jdk8"))
    compile(files("../libs/paper_server.jar"))
    compile(files("../libs/DreamCore-shadow.jar"))
    implementation("net.citizensnpcs:citizens:2.0.26-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
