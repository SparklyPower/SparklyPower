import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.exposed:exposed-core:0.38.2")
    api("org.jetbrains.exposed:exposed-dao:0.38.2")
    api("org.jetbrains.exposed:exposed-jdbc:0.38.2")
    api("net.perfectdreams.exposedpowerutils:exposed-power-utils:1.1.0")
    api("net.perfectdreams.exposedpowerutils:postgres-java-time:1.1.0")
    api("net.perfectdreams.exposedpowerutils:postgres-power-utils:1.1.0")
    api("dev.forst:exposed-upsert:1.2.0")
    api("com.zaxxer:HikariCP:5.0.1")
    api("org.postgresql:postgresql:42.3.6")
    api("com.michael-bull.kotlin-coroutines-jdbc:kotlin-coroutines-jdbc:1.0.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}