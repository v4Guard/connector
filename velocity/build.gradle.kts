plugins {
    id("connector.common-conventions")
    id("connector.shadow-conventions")
}

dependencies {
    annotationProcessor(libs.velocity)
    compileOnly(libs.velocity)
    compileOnly(libs.floodgate)

    implementation(libs.bstats.velocity)
    implementation(libs.commandflow.velocity) {
        exclude(group = "net.kyori", module = "adventure-api")
        exclude(group = "net.kyori", module = "adventure-text-serializer-gson")
        exclude(group = "net.kyori", module = "adventure-text-serializer-legacy")
        exclude(group = "net.kyori", module = "adventure-text-serializer-plain")
    }

    implementation(project(":commons"))
}