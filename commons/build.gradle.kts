plugins {
    id("connector.common-conventions")
    id("maven-publish")
}

dependencies {
    api(project(":api"))
    compileOnly(libs.bungeecord)
    implementation(libs.socketio)
    implementation(libs.caffeine)
    implementation(libs.jackson.databind)
}
