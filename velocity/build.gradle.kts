plugins {
    id("connector.common-conventions")
    id("connector.shadow-conventions")
}

dependencies {
    annotationProcessor(libs.velocity)
    compileOnly(libs.velocity)
    compileOnly(libs.floodgate)

    implementation(libs.bstats.velocity)
    implementation(libs.commandflow.velocity)
    implementation(project(":commons"))
}