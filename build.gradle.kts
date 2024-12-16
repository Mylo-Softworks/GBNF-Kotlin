plugins {
    kotlin("multiplatform") version "2.0.20"
    id("maven-publish")
}

group = "com.mylosoftworks"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {

}

publishing {

}

kotlin {
    jvm()
    js {
        browser()
        nodejs()
    }
}