import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.perfectdreams.net/")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    compileOnly(files("../../libs/paper_server.jar"))
    compileOnly("io.github.waterfallmc:waterfall-api:1.13-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.5.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
    api(kotlin("reflect"))
    api(kotlin("script-util"))
    api(kotlin("compiler"))
    api(kotlin("scripting-compiler"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    api("io.github.microutils:kotlin-logging-jvm:2.0.10")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("KotlinRuntime-shadow")

        exclude {
            it.file?.name?.startsWith("paper_server") == true || it.file?.name?.startsWith("waterfall") == true
        }
    }

    "build" {
        dependsOn(shadowJar)
    }
}