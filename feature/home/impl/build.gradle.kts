plugins {
    alias(libs.plugins.convention.android.feature)
}

android {
    namespace = "com.ruma.repnote.feature.home"
}

dependencies {
    implementation(projects.core.auth.api)
    implementation(projects.core.domain)

    implementation(projects.feature.auth.api)
}
