package com.ruma.buildlogic.convention.logic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.ruma.buildlogic.convention.logic.constants.IMPLEMENTATION
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(extension: ApplicationExtension) {
    extension.apply {
        compileSdk = getCompileSdk()

        defaultConfig {
            minSdk = getMinSdk()
        }

        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(getJavaVersion())
            targetCompatibility = JavaVersion.toVersion(getJavaVersion())
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "META-INF/LICENSE.md"
                excludes += "META-INF/LICENSE"
                excludes += "META-INF/LICENSE.txt"
                excludes += "META-INF/NOTICE"
                excludes += "META-INF/NOTICE.txt"
                excludes += "META-INF/LICENSE-notice.md"
            }
        }
    }

    configureKotlinAndroidDependencies()
    configureKotlin()
}

internal fun Project.configureKotlinAndroid(extension: LibraryExtension) {
    extension.apply {
        compileSdk = getCompileSdk()

        defaultConfig {
            minSdk = getMinSdk()
        }

        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(getJavaVersion())
            targetCompatibility = JavaVersion.toVersion(getJavaVersion())
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
                excludes += "META-INF/LICENSE.md"
                excludes += "META-INF/LICENSE"
                excludes += "META-INF/LICENSE.txt"
                excludes += "META-INF/NOTICE"
                excludes += "META-INF/NOTICE.txt"
                excludes += "META-INF/LICENSE-notice.md"
            }
        }
    }

    configureKotlinAndroidDependencies()
    configureKotlin()
}

private fun Project.configureKotlinAndroidDependencies() {
    dependencies {
        add(IMPLEMENTATION, libs.findLibrary("androidx-core-ktx").get())
        add(IMPLEMENTATION, libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
        add(IMPLEMENTATION, libs.findLibrary("androidx-lifecycle-viewmodel-navigation").get())
    }
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.values()[getJavaVersion() - 1]
        targetCompatibility = JavaVersion.values()[getJavaVersion() - 1]
    }

    configureKotlin()
}

private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(getJavaVersion().toString()))
        }
    }
}
