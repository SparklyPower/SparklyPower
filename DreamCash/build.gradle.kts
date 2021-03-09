import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly("net.luckperms:api:5.0")
    compileOnly(project(":DreamClubes"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
