import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven {
        url = uri("https://repository.hellonico.info/repository/hellonico/")
    }
}

dependencies {
    implementation("org.bytedeco:javacv:1.4.4")
    implementation("org.bytedeco.javacpp-presets:ffmpeg:4.1-1.4.4")
    implementation("org.bytedeco:javacv-platform:1.4.4")

    implementation("com.github.sarxos:webcam-capture:0.3.12")

    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.getByName<Zip>("distZip").enabled = false
tasks.getByName<Tar>("distTar").enabled = false

tasks.jar {
    manifest {
        attributes("Main-Class" to "MainKt")
    }
}
