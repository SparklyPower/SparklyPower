import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":bukkit:DreamCore"))
    implementation("net.citizensnpcs:citizens:2.0.26-SNAPSHOT")
    implementation("net.luckperms:api:5.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}