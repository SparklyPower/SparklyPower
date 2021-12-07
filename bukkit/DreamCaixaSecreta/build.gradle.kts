import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    paperweightDevBundle(SparklyPaperDevBundle.GROUP, SparklyPaperDevBundle.VERSION)
    compileOnly(project(":bukkit:DreamCore"))
    compileOnly(project(":bukkit:DreamCash"))
    compileOnly(files("../../libs/NoteBlockAPI.jar"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    // Configure reobfJar to run when invoking the build task
    build {
        dependsOn(reobfJar)
    }
}