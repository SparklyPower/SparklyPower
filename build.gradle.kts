import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10" apply false
    id("io.papermc.paperweight.userdev") version Versions.PAPERWEIGHT_USERDEV apply false
}

group = "net.perfectdreams.sparklypower"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://nexus.wesjd.net/repository/thirdparty/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-snapshots/") // ChangeSkin
        maven("https://jitpack.io")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.citizensnpcs.co/")
        maven("https://maven.fabricmc.net") // Required by paperweight userdev
        maven("https://repo.viaversion.com")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
