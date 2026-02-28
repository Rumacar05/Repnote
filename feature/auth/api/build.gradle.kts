plugins {
    alias(libs.plugins.convention.jvm.library)
}

dependencies {
    api(project(":core:auth:api"))
    api(libs.coroutines.core)
}
