import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":bungee:DreamCoreBungee"))
    compileOnly("net.luckperms:api:5.2")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
