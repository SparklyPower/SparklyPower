import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

println(JavaVersion.current())

plugins {
    kotlin("jvm") version "1.4.20"
}

group = "net.perfectdreams.sparklypower"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    repositories {
        mavenLocal()

        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
        maven("https://ci.dmulloy2.net/nexus/repository/public/")
        maven("https://nexus.wesjd.net/repository/thirdparty/")
        maven("https://maven.sk89q.com/repo/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://jitpack.io")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.citizensnpcs.co/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
