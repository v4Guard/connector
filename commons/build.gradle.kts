plugins {
    id("connector.common-conventions")
}

dependencies {
    api(project(":api"))

    compileOnly(libs.bungeecord)
    compileOnly(libs.bundles.adventure)

    listOf(
        libs.socketio, libs.caffeine, libs.jackson.databind
    ).forEach {
        implementation(it)
    }
}
