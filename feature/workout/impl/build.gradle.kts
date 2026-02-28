plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.ruma.repnote.feature.workout"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.stringResources)
    implementation(projects.core.auth.api)
}
