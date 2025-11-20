import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val minSdkProperty: String by project
val compileSdkProperty: String by project

android {
    namespace = "com.magnite.demo.compose"
    compileSdk = compileSdkProperty.toInt()

    defaultConfig {
        applicationId = "com.magnite.demo.compose"
        minSdk = minSdkProperty.toInt()
        targetSdk = compileSdkProperty.toInt()
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("com.magnite.sdk:ads-sdk:[0,1)")

    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.material3:material3:1.4.0")
}
