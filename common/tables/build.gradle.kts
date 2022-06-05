import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.exposed:exposed-core:0.38.2")
    api("org.jetbrains.exposed:exposed-dao:0.38.2")
    api("org.jetbrains.exposed:exposed-jdbc:0.38.2")
    api("net.perfectdreams.exposedpowerutils:postgres-java-time:1.0.0")
    api("dev.forst:exposed-upsert:1.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}