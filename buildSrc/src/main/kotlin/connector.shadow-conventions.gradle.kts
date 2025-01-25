plugins {
    id("io.github.goooler.shadow")
    id("connector.common-conventions")
}

tasks {
    shadowJar {
        destinationDirectory.set(file("$rootDir/out"))
        archiveFileName.set("v4Guard-connector-${project.name}-${project.version}.jar")

        val prefix = "io.v4guard.connector.libs"

        //realocations for shadowJar
        listOf(
            "okio", "okhttp3", "io.socket", "org.json",
            "org.checkerframework", "org.bstats", "com.fasterxml.jackson",
            "com.google.errorprone.annotations", "com.github.benmanes.caffeine.cache"
        ).forEach { relocate(it, "${prefix}.$it") }
    }

    named("assemble") {
        dependsOn("shadowJar")
    }
}