plugins {
    kotlin("multiplatform") version "1.6.20"
}
group = "de.atennert"

repositories {
    mavenCentral()
}
kotlin {
    val nativeTarget = when (System.getProperty("os.name")) {
        "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
        compilations.getByName("main") {
            val statusbar by cinterops.creating
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            }
        }
        commonTest {
            dependencies {
                implementation( "org.jetbrains.kotlin:kotlin-test-common")
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}
