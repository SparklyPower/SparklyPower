import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("../libs/patched_1.15.1.jar"))
    implementation(files("../libs/DreamCore-shadow.jar"))
    implementation(files("../libs/WorldGuard.jar"))
    implementation(files("../libs/ChestShop.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
