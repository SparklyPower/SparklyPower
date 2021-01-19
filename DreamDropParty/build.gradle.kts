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
    compileOnly(files("../libs/paper_server.jar"))
    compile(files("../libs/DreamCore-shadow.jar"))
    compile(project(":DreamChat"))
    compile(project(":DreamCash"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}