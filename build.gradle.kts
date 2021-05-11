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

        maven("http://nexus.hc.to/content/repositories/pub_releases")
        maven("https://ci.dmulloy2.net/nexus/repository/public/")
        maven("https://dl.bintray.com/kotlin/kotlin-dev/")
        maven("https://dl.bintray.com/kotlin/kotlin-eap/")
        maven("https://jcenter.bintray.com")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.perfectdreams.net/")
        maven("https://dl.bintray.com/kotlin/exposed/")
        maven("https://nexus.wesjd.net/repository/thirdparty/")
        // maven("http://nexus.okkero.com/repository/maven-releases/")
        maven("https://maven.sk89q.com/repo/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://dl.bintray.com/ichbinjoe/public/")
        maven("https://repo.codemc.org/repository/maven-public")
        // maven("http://repo.ess3.net/content/repositories/bukkitsnapshot/")
        maven("https://jitpack.io")
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
