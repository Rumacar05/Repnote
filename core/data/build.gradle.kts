plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.koin)
}

android {
    namespace = "com.ruma.repnote.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.database)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation(libs.cloudinary.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // WorkManager for background sync
    implementation(libs.work.runtime.ktx)
}
