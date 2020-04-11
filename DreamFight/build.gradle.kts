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
    compile(files("../libs/patched_1.15.1.jar"))
    compile(files("../libs/DreamCore-shadow.jar"))
    compile(project(":DreamChat"))
    compile(project(":DreamCash"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
