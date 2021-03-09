import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compileOnly(project(":DreamCash"))
    compileOnly(project(":DreamCorreios"))
    compileOnly(project(":DreamCasamentos"))
    compileOnly(project(":DreamClubes"))
    compileOnly(project(":DreamVanish"))
    compileOnly(files("../libs/mcMMO.jar"))
    compileOnly("net.luckperms:api:5.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
