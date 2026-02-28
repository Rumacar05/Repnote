plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.ruma.repnote.feature.auth"
}

dependencies {
    implementation(projects.core.analytics.impl)
    api(projects.feature.auth.api)
    implementation(projects.core.auth.api)
}
