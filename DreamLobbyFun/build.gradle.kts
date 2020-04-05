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
    compile("com.destroystokyo.paper:paper:1.13-R0.1-SNAPSHOT")
    compile(files("../libs/DreamCore-shadow.jar"))
    compile(files("../libs/NoteBlockAPI.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
