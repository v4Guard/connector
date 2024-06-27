plugins {
    id("connector.common-conventions")
    id("connector.shadow-conventions")
}

dependencies {
    compileOnly(libs.bungeecord)
    compileOnly(libs.floodgate)
    compileOnly(libs.caffeine)

    implementation(libs.bstats.bungeecord)
    implementation(project(":commons"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
