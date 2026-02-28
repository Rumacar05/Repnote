plugins {
    alias(libs.plugins.convention.android.library.compose)
    alias(libs.plugins.convention.paparazzi)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ruma.repnote.core.designsystem"

    ksp {
        arg("skipPrivatePreviews", "true")
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.stringResources)
}
