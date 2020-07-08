import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(project(":DreamCash"))
    compileOnly(files("../libs/patched_1.15.1.jar"))
    compile(files("../libs/DreamCore-shadow.jar"))
    compile("net.perfectdreams.dreamchat:DreamChat:1.0-SNAPSHOT")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
