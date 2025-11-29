@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

group = "starry.codec"
version = "1.0.0"

kotlin {
    mingwX64()
    linuxX64()
    linuxArm64()
    iosX64()
    iosArm64()

    js {
        browser()
        nodejs()
    }

    wasmJs {
        browser()
        nodejs()
        d8()
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    sourceSets {
        commonMain.dependencies {
            compileOnly(libs.arrow.core)
            compileOnly(libs.bundles.kotlinx.ecosystem)
            api(kotlin("reflect"))
        }

        commonTest.dependencies {
            api(kotlin("test"))
        }
    }
}
