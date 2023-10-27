import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
    api(kotlin("stdlib"))
    paperDevBundle("1.20.2-R0.1-SNAPSHOT")
    compileOnly("io.github.waterfallmc:waterfall-api:1.13-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.6")
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.0-RC")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}