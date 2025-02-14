import org.gradle.kotlin.dsl.support.zipTo
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.8.0"
	id("org.jetbrains.compose")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	google()
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	maven("https://repository.hellonico.info/repository/hellonico/")
}

dependencies {
	implementation("org.bytedeco:javacv:1.4.4")
	implementation("org.bytedeco.javacpp-presets:ffmpeg:4.1-1.4.4")
	implementation("org.bytedeco:javacv-platform:1.4.4")

	implementation("com.github.sarxos:webcam-capture:0.3.12")

	implementation(kotlin("stdlib-jdk8"))

	//compose
	implementation(compose.desktop.currentOs)

	//koin
	implementation("io.insert-koin:koin-core:3.1.5")
	implementation("io.insert-koin:koin-test:3.1.5")

	implementation(project(":ui"))
}

tasks.test {
	useJUnitPlatform()
}

kotlin {
	jvmToolchain(8)
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
	jvmTarget = "1.8"
}

tasks.jar {
	manifest {
		attributes("Main-Class" to "MainKt")
	}
}

//tasks.getByName<Zip>("distZip").enabled = false
//tasks.getByName<Tar>("distTar").enabled = false

// Чтобы сделать exe ./gradlew :createDistributable
// файл будет в $rootDir\build\compose\binaries\main\app

compose.desktop {
	application {
		mainClass = "MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "necoscii"
			packageVersion = "1.0.0"
			windows {
				// a version for all Windows distributables
				packageVersion = "1.0.0"
				// a version only for the msi package
				msiPackageVersion = "1.0.0"
				// a version only for the exe package
				exePackageVersion = "1.0.0"
			}
		}
	}
}