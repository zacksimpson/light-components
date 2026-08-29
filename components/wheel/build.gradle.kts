// Only builds when included as a subproject of a light-sdk checkout, see the root
// README for the local-only settings.gradle.kts wiring. Never published, this file just
// lets you compile-check the component against a real SDK before copying it out.

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.thelightphone.components.wheel"
    compileSdk = rootProject.ext["compileSdk"] as Int

    defaultConfig {
        minSdk = rootProject.ext["minSdk"] as Int
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(rootProject.ext["jvmTarget"] as String)
        targetCompatibility = JavaVersion.toVersion(rootProject.ext["jvmTarget"] as String)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(rootProject.ext["jvmTarget"] as String))
    }
}

dependencies {
    implementation(project(":sdk:client"))
    // The wheel arrives as raw key events; recognition lives in the hardware-keys component,
    // wired in the same way — see the root README for the settings.gradle.kts lines.
    implementation(project(":hardware-keys"))
    testImplementation(libs.kotlin.test)
}
