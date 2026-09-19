plugins {
    id("connector.common-conventions")
    id("connector.shadow-conventions")
}

dependencies {
    annotationProcessor(libs.velocity)
    compileOnly(libs.floodgate)

    // To have access to a few internal velocity classes, we have to include the whole velocity jar...
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly(libs.velocity)

    listOf(
        libs.bstats.velocity,
        libs.cloud.velocity,
        libs.cloud.annotations,
        project(":commons")
    ).forEach {
        implementation(it)
    }
}