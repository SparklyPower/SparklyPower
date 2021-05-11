import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":bukkit:DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly(project(":bukkit:DreamChat"))
    compileOnly(project(":bukkit:DreamCash"))
    compileOnly(project(":bukkit:DreamCorreios"))
    compileOnly("com.vexsoftware:nuvotifier-bukkit:2.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
