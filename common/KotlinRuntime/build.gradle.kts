import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    paperweightDevBundle(SparklyPaperDevBundle.GROUP, SparklyPaperDevBundle.VERSION)
    compileOnly("io.github.waterfallmc:waterfall-api:1.13-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    api(kotlin("reflect"))
    api(kotlin("script-util"))
    api(kotlin("compiler"))
    api(kotlin("scripting-compiler"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.0-RC")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")
    api("io.github.microutils:kotlin-logging-jvm:2.1.21")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}