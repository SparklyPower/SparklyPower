import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly(project(":DreamCash"))
    compileOnly(project(":DreamChat"))
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
