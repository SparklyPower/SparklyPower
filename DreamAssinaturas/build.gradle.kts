import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(files("../libs/paper_server.jar"))
    compile(files("../libs/DreamCore-shadow.jar"))
    compile(files("../libs/WorldGuard.jar"))
    implementation("com.github.ChestShop-authors:ChestShop-3:3.11")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
