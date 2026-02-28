plugins {
    alias(libs.plugins.convention.android.feature)
}

android {
    namespace = "com.ruma.repnote.feature.profile"
}

dependencies {
    implementation(projects.core.analytics.impl)
    implementation(projects.core.auth.api)
    implementation(projects.core.stringResources)
    implementation(projects.feature.auth.api)
}
