import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.citizensnpcs.co/")
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly(project(":DreamAuth"))
    compileOnly(files("../libs/NoteBlockAPI.jar"))
    implementation("net.citizensnpcs:citizens:2.0.26-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
