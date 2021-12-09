import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.exposed:exposed-core:0.36.2")
    api("org.jetbrains.exposed:exposed-dao:0.36.2")
    api("org.jetbrains.exposed:exposed-jdbc:0.36.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}