import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":bukkit:DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly(project(":bukkit:DreamCash"))
    compileOnly(project(":bukkit:DreamCorreios"))
    compileOnly(project(":bukkit:DreamCasamentos"))
    compileOnly(project(":bukkit:DreamClubes"))
    compileOnly(project(":bukkit:DreamVanish"))
    compileOnly(files("../../libs/mcMMO.jar"))
    compileOnly("net.luckperms:api:5.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
