plugins {
    alias(libs.plugins.convention.android.feature)
    alias(libs.plugins.convention.paparazzi)
}

android {
    namespace = "com.ruma.repnote.feature.routine"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.stringResources)
    implementation(projects.core.auth.api)

    implementation(libs.bundles.coil)

    implementation(libs.reorderable)
}
