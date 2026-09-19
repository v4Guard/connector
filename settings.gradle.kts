rootProject.name = "connector"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

listOf(
    "api", "commons", "velocity", "bungee"
).forEach { project ->
    include(project)
}