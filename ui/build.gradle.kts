import org.gradle.initialization.GradlePropertiesController
import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	id("org.jetbrains.compose")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation(compose.desktop.currentOs)
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(11)
}

// Включает возможность использовать context receiver
tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xcontext-receivers"
	}
}