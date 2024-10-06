plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("me.alllex.parsus:parsus-jvm:0.6.1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

sourceSets["main"].kotlin {
    srcDir("src/main/kotlin")
    //srcDir("src/parsus/kotlin")
}