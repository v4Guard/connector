plugins {
    id("connector.common-conventions")
}

dependencies {
    compileOnly(libs.bungeecord)
    implementation(libs.socketio)
    implementation(libs.caffeine)
    implementation(libs.jackson.databind)
}