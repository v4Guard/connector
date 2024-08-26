plugins {
    id("connector.common-conventions")
    id("maven-publish")
}

dependencies {
    compileOnly(libs.bungeecord)
    implementation(libs.socketio)
    implementation(libs.caffeine)
    implementation(libs.jackson.databind)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}