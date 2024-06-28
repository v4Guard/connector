plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("io.github.goooler.shadow:shadow-gradle-plugin:8.1.7")
}


tasks {
    compileKotlin {
        kotlinOptions {
            //change version
            jvmTarget = "11"
        }
    }

}