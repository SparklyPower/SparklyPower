plugins {
    kotlin("jvm")
}

dependencies {
    implementation("net.kyori:adventure-api:4.14.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.14.0")

    api("io.github.oshai:kotlin-logging:5.1.0")

    // Only because some things still require the old version
    api("io.github.microutils:kotlin-logging:2.1.23")
}