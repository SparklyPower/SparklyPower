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
    compile(files("../libs/DreamCorreios.jar"))
    compile(project(":DreamChat"))
    compile("net.milkbowl.vault:VaultAPI:1.6")
    compile("com.vexsoftware:nuvotifier-bukkit:2.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
