import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.1")
    compile(project(":DreamCash"))
    compile(files("../libs/patched_1.15.1.jar"))
    compile("net.perfectdreams.dreamcore:DreamCore:1.0-SNAPSHOT")
    compile("net.perfectdreams.dreamchat:DreamChat:1.0-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
