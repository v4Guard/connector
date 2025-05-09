plugins {
    id("connector.common-conventions")
    id("maven-publish")
}

dependencies {
    compileOnly(libs.jackson.databind)
}


java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}