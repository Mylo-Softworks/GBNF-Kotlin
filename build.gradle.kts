plugins {
    kotlin("multiplatform") version "2.0.20"
}

group = "com.mylosoftworks"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}

kotlin {
    jvm()
    js()
}