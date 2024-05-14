import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kover)
}

group = "com.kotlinconf.workshop"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(projects.shared)
                implementation(compose.desktop.currentOs)
                implementation(compose.materialIconsExtended)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.retrofit)
                implementation(libs.retrofit.mock)
                implementation(libs.retrofit.serialization)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)

                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.websockets)
                implementation(libs.okhttp)
                implementation(libs.kotlinx.datetime)
                implementation(libs.logback.classic)
                implementation(libs.kotlinx.collections.immutable)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.junit.jupiter)
                implementation(libs.turbine)

            }
        }
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.kotlinconf.workshop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "desktop-client"
            packageVersion = "1.0.0"
        }
    }
}
