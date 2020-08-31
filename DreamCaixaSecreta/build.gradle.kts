import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compileOnly(files("../libs/patched_1.15.1.jar"))
    compileOnly(project(":DreamCash"))
    compile(files("../libs/DreamCore-shadow.jar"))
    compile(files("../libs/NoteBlockAPI.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
