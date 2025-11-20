plugins {
    id("com.android.application")
}

val minSdkProperty: String by project
val compileSdkProperty: String by project

android {
    namespace = "com.magnite.demo.java"
    compileSdk = compileSdkProperty.toInt()

    defaultConfig {
        applicationId = "com.magnite.demo.java"
        minSdk = minSdkProperty.toInt()
        targetSdk = compileSdkProperty.toInt()
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("com.magnite.sdk:ads-sdk:[0,1)")

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.jakewharton:process-phoenix:3.0.0")
}
