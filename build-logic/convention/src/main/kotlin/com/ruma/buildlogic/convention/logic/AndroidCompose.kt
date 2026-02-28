package com.ruma.buildlogic.convention.logic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.ruma.buildlogic.convention.logic.constants.ANDROID_TEST_IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.constants.DEBUG_IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.constants.IMPLEMENTATION
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(extension: ApplicationExtension) {
    extension.apply {
        buildFeatures {
            compose = true
        }
    }
    configureComposeDependencies()
}

internal fun Project.configureAndroidCompose(extension: LibraryExtension) {
    extension.apply {
        buildFeatures {
            compose = true
        }
    }
    configureComposeDependencies()
}

private fun Project.configureComposeDependencies() {
    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add(IMPLEMENTATION, platform(bom))
        add(ANDROID_TEST_IMPLEMENTATION, platform(bom))
        add(IMPLEMENTATION, libs.findBundle("compose").get())
        add(DEBUG_IMPLEMENTATION, libs.findBundle("compose-debug").get())
        add(
            ANDROID_TEST_IMPLEMENTATION,
            libs.findLibrary("androidx-compose-ui-test-junit4").get(),
        )
    }
}