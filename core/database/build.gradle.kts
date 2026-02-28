plugins {
    alias(libs.plugins.convention.android.library)
    alias(libs.plugins.convention.android.koin)
}

android {
    namespace = "com.ruma.repnote.core.database"
}

dependencies {
    api(projects.core.domain)

    // Room - need to add ksp for Room compiler since the koin plugin adds ksp
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    add("ksp", libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.core)

    // Serialization for TypeConverters
    implementation(libs.kotlinx.serialization.json)
}
