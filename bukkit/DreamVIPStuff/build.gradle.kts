import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(project(":bukkit:DreamCore", configuration = "shadowWithRuntimeDependencies"))
    implementation("net.citizensnpcs:citizens:2.0.26-SNAPSHOT")
    implementation("net.luckperms:api:5.0")
    implementation("org.jsoup:jsoup:1.13.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}