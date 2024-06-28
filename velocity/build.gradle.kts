plugins {
    id("connector.common-conventions")
    id("connector.shadow-conventions")
}

dependencies {
    annotationProcessor(libs.velocity)
    compileOnly(libs.velocity)
    compileOnly(libs.floodgate)

    implementation(libs.bstats.velocity)
    implementation(project(":commons"))
}