import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.sergiodev.bingo"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material3)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspDesktop", libs.androidx.room.compiler)
}

compose.resources {
    packageOfResClass = "com.sergiodev.bingo.resources"
    publicResClass = false
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "com.sergiodev.bingo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "BingoFF"
            packageVersion = "1.0.0"
            description = "BingoFF desktop"
        }
    }
}

// KMP projects have no lifecycle `test` task; alias it so `./gradlew test` runs the JVM desktop tests.
tasks.register("test") {
    group = "verification"
    description = "Runs all unit tests (alias for desktopTest)."
    dependsOn("desktopTest")
}
