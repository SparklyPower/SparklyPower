import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0" apply false
    id("io.papermc.paperweight.userdev") version Versions.PAPERWEIGHT_USERDEV apply false
}

group = "net.perfectdreams.sparklypower"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(60, TimeUnit.SECONDS)
    }
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://nexus.wesjd.net/repository/thirdparty/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-snapshots/") // ChangeSkin
        maven("https://jitpack.io")
        maven("https://maven.citizensnpcs.co/repo")
        maven("https://maven.fabricmc.net") // Required by paperweight userdev
        maven("https://repo.viaversion.com")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
