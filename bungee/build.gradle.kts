plugins {
    id("connector.common-conventions")
    id("connector.shadow-conventions")
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

extra["bungeecord"] = true

dependencies {
    //compileOnly(libs.bungeecord)

    listOf(
        libs.floodgate,
        libs.caffeine,
        libs.bundles.adventure,
        fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar")))
    ).forEach { cDep -> compileOnly(cDep) }

    listOf(
        libs.bstats.bungeecord,
        libs.cloud.bungeecord,
        libs.cloud.annotations,
        libs.bundles.adventure.bungeecord,
        project(":commons")
    ).forEach { iDep -> implementation(iDep) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

bungee {
    name = "v4guard-plugin"
    main = "io.v4guard.connector.platform.bungee.BungeeInstance"
    version = project.version.toString()
    author = "v4Guard"
    description = "v4Guard connector for BungeeCord platform"
}
