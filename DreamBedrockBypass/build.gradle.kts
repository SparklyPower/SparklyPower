import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/releases/")
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly("fr.neatmonster:nocheatplus:3.16.1-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
