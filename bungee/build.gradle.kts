plugins {
    id("connector.common-conventions")
    id("connector.shadow-conventions")
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

dependencies {
    //compileOnly(libs.bungeecord)
    compileOnly(libs.floodgate)
    compileOnly(libs.caffeine)

    implementation(libs.commandflow.bungeecord)
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.bstats.bungeecord)
    implementation(project(":commons"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

bungee {
    name = "v4guard-plugin"
    main = "io.v4guard.connector.platform.bungee.BungeeInstance"
    version = project.version.toString()
    description = "v4Guard connector for BungeeCord platform"
}
