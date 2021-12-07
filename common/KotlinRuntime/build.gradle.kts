import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    paperweightDevBundle(SparklyPaperDevBundle.GROUP, SparklyPaperDevBundle.VERSION)
    compileOnly("io.github.waterfallmc:waterfall-api:1.13-SNAPSHOT")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.5.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
    api(kotlin("reflect"))
    api(kotlin("script-util"))
    api(kotlin("compiler"))
    api(kotlin("scripting-compiler"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    api("io.github.microutils:kotlin-logging-jvm:2.0.10")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveBaseName.set("KotlinRuntime-shadow")

        exclude {
            it.file?.name?.startsWith("paper_server") == true || it.file?.name?.startsWith("waterfall") == true
        }
    }

    reobfJar {
        // For some reason the userdev plugin is using "unspecified" as the suffix, and that's not a good name
        // So we are going to change it to "PluginName-reobf.jar"
        outputJar.set(layout.buildDirectory.file("libs/${project.name}-reobf.jar"))
    }

    "build" {
        dependsOn(shadowJar)
        dependsOn(reobfJar)
    }
}