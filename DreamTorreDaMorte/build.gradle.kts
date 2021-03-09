import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly(project(":DreamChat"))
    compileOnly(project(":DreamCash"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
