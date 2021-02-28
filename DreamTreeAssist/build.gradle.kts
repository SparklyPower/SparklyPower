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
    implementation(files("../libs/paper_server.jar"))
    implementation(files("../libs/DreamCore-shadow.jar"))
    compile(files("../libs/mcMMO.jar"))
    compileOnly("com.github.TechFortress:GriefPrevention:16.17.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
