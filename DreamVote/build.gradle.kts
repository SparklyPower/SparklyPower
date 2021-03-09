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
    compileOnly(project(":DreamCorreios"))
    compileOnly("com.vexsoftware:nuvotifier-bukkit:2.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
