import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    compile(files("../libs/mcMMO.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
