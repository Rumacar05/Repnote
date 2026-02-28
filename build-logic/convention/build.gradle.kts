plugins {
    `kotlin-dsl`
}

group = "com.ruma.repnote.buildlogic.convention"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("android-application") {
            id = "com.ruma.repnote.buildlogic.convention.android-application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("android-library") {
            id = "com.ruma.repnote.buildlogic.convention.android-library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("android-library-compose") {
            id = "com.ruma.repnote.buildlogic.convention.android-library-compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("android-feature") {
            id = "com.ruma.repnote.buildlogic.convention.android-feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("jvm-library") {
            id = "com.ruma.repnote.buildlogic.convention.jvm-library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("android-koin") {
            id = "com.ruma.repnote.buildlogic.convention.android-koin"
            implementationClass = "AndroidKoinConventionPlugin"
        }
        register("jvm-koin") {
            id = "com.ruma.repnote.buildlogic.convention.jvm-koin"
            implementationClass = "JvmKoinConventionPlugin"
        }
        register("android-test") {
            id = "com.ruma.repnote.buildlogic.convention.android-test"
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("jvm-test") {
            id = "com.ruma.repnote.buildlogic.convention.jvm-test"
            implementationClass = "JvmTestConventionPlugin"
        }
        register("paparazzi") {
            id = "com.ruma.repnote.buildlogic.convention.paparazzi"
            implementationClass = "PaparazziConventionPlugin"
        }
        register("detektConvention") {
            id = "com.ruma.repnote.buildlogic.convention.detekt"
            implementationClass = "DetektConventionPlugin"
        }
        register("ktlintConvention") {
            id = "com.ruma.repnote.buildlogic.convention.ktlint"
            implementationClass = "KtlintConventionPlugin"
        }
    }
}
