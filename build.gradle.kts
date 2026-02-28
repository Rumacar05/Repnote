plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.convention.detekt)
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.paparazzi) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

subprojects {
    apply {
        plugin("com.ruma.repnote.buildlogic.convention.ktlint")
    }
}

apply(from = "gradle-scripts/copyGitHooks.gradle.kts")
