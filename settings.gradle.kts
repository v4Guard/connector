rootProject.name = "connector"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

listOf(
    "commons", "velocity", "bungee"
).forEach { project ->
    include(project)
}