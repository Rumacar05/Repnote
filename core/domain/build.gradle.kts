plugins {
    alias(libs.plugins.convention.jvm.library)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
