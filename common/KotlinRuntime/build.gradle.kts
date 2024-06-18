import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    api(kotlin("stdlib"))
    paperweight.devBundle("net.sparklypower.sparklypaper", "1.21-R0.1-SNAPSHOT")
    compileOnly("io.github.waterfallmc:waterfall-api:1.13-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.6")
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.0-RC")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0-RC")
}
