plugins {
    id("connector.common-conventions")
}

dependencies {
    api(project(":api"))
    compileOnly(libs.bungeecord)
    implementation(libs.socketio)
    implementation(libs.caffeine)
    implementation(libs.jackson.databind)
}
