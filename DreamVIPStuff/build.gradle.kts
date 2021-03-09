import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.citizensnpcs.co/")
}

dependencies {
    compileOnly(project(":DreamCore", configuration = "shadowWithRuntimeDependencies"))
    implementation("net.citizensnpcs:citizens:2.0.26-SNAPSHOT")
    implementation("net.luckperms:api:5.0")
    implementation("org.jsoup:jsoup:1.13.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val fatJar = task("fatJar", type = Jar::class) {
        println("Building fat jar for ${project.name}...")

        archiveBaseName.set("${project.name}-fat")

        from(configurations.runtimeClasspath.get().mapNotNull {
            if (it.name.contains("jsoup"))
                zipTree(it)
            else
                null
        })

        with(jar.get() as CopySpec)
    }

    "build" {
        dependsOn(fatJar)
    }
}