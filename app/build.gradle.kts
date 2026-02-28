import java.util.Properties

plugins {
    alias(libs.plugins.convention.android.application)
    alias(libs.plugins.convention.android.koin)
    alias(libs.plugins.google.services)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.ruma.repnote"

    defaultConfig {
        applicationId = "com.ruma.repnote"

        resValue("string", "app_name", "Repnote")

        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${localProperties.getProperty("cloudinary.cloud.name", "")}\"",
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_KEY",
            "\"${localProperties.getProperty("cloudinary.api.key", "")}\"",
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_SECRET",
            "\"${localProperties.getProperty("cloudinary.api.secret", "")}\"",
        )
    }

    buildTypes {
        release {
        }
    }

    buildFeatures {
        buildConfig = true
        resValues = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    // Core modules
    implementation(projects.core.designSystem)
    implementation(projects.core.stringResources)
    implementation(projects.core.auth.impl)
    implementation(projects.core.data)
    implementation(projects.core.database)

    // Feature modules
    implementation(projects.feature.auth.impl)
    implementation(projects.feature.home.impl)
    implementation(projects.feature.routine.impl)
    implementation(projects.feature.profile.impl)
    implementation(projects.feature.workout.impl)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    implementation(libs.koin.androidx.workmanager)

    // Cloudinary
    implementation(libs.cloudinary.android)

    // WorkManager
    implementation(libs.work.runtime.ktx)
}
