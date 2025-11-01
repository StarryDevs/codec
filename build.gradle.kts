@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.2.20"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "starry.codec"
version = "1.0.0"

kotlin {
    wasmJs {
        browser()
        nodejs()
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("io.arrow-kt:arrow-core:2.2.0-beta.11")
            implementation(kotlin("reflect"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
