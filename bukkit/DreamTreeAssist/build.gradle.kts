import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(project(":bukkit:DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly(files("../../libs/mcMMO.jar"))
    compileOnly("com.github.TechFortress:GriefPrevention:16.17.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}