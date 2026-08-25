plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

group = "com.remmi"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("database_scripts.DatabaseUpdater")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.2.2")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.2.2")
    implementation("io.ktor:ktor-client-cio:3.1.1")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.18")
}
