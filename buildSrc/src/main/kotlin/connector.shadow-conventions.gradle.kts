plugins {
    id("com.gradleup.shadow")
    id("connector.common-conventions")
}

tasks {
    shadowJar {
        destinationDirectory.set(file("$rootDir/out"))
        archiveFileName.set("v4Guard-connector-${project.name}-${project.version}.jar")

        val prefix = "io.v4guard.connector.libs"

        //relocations for shadowJar
        val relocations = mutableListOf(
            "okio", "okhttp3", "io.socket", "org.json",
            "org.checkerframework", "org.bstats", "com.fasterxml.jackson",
            "com.google.errorprone.annotations", "com.github.benmanes.caffeine.cache",
            "org.incendo.cloud", "org.jetbrains.annotations", "org.intellij.lang.annotations",
            "io.leangen.geantyref", "org.jspecify.annotations",
        )

        if (project.hasProperty("bungeecord")) {
            // bungeecord related dependencies
            relocations.addAll(listOf("net.kyori.adventure", "net.kyori.examination", "net.kyori.option", "com.google.gson"))
        }

        relocations.forEach { relocate(it, "${prefix}.$it") }
    }

    named("assemble") {
        dependsOn("shadowJar")
    }
}