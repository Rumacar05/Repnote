plugins {
    alias(libs.plugins.convention.android.library.compose)
    alias(libs.plugins.convention.android.koin)
    alias(libs.plugins.convention.android.test)
}

android {
    namespace = "com.ruma.repnote.core.analytics.impl"
}

dependencies {
    api(projects.core.analytics.api)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Koin for Compose (needed for koinInject in TrackScreenView)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.androidx.compose)
}
