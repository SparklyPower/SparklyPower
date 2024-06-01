import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("../../libs/paper_server.jar"))
    implementation(files("../../libs/DreamCore-shadow.jar"))
    implementation(files("../../libs/WorldGuard.jar"))
}
