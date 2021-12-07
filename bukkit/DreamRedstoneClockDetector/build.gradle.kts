import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.citizensnpcs.co/")
}

dependencies {
    paperweightDevBundle(SparklyPaperDevBundle.GROUP, SparklyPaperDevBundle.VERSION)
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("../../libs/paper_server.jar"))
    implementation(files("../../libs/DreamCore-shadow.jar"))
    implementation(files("../../libs/WorldGuard.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}