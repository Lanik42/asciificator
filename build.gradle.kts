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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("opencv:opencv:4.0.1-1")
//    implementation("opencv:opencv:4.5.0-0")

    implementation("com.github.jbellis:jamm:0.4.0")

    implementation("org.bytedeco:javacv:1.4.4")
    implementation("org.bytedeco.javacpp-presets:ffmpeg:4.1-1.4.4")
    implementation("org.bytedeco:javacv-platform:1.4.4")
    api("com.aparapi:aparapi:3.0.0")
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