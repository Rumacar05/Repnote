plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.koin)
}

android {
    namespace = "com.ruma.repnote.core.auth.impl"
}

dependencies {
    // Core auth API
    api(project(":core:auth:api"))

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Coroutines for Firebase
    implementation(libs.kotlinx.coroutines.play.services)
}
