plugins {
	kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven("https://repository.hellonico.info/repository/hellonico/")
}

dependencies {
	implementation("org.bytedeco:javacv:1.4.4")
	implementation("org.bytedeco.javacpp-presets:ffmpeg:4.1-1.4.4")
	implementation("org.bytedeco:javacv-platform:1.4.4")

	implementation("com.github.sarxos:webcam-capture:0.3.12")
}

kotlin {
	jvmToolchain(11)
}

//tasks.getByName<Zip>("distZip").enabled = false
//tasks.getByName<Tar>("distTar").enabled = false