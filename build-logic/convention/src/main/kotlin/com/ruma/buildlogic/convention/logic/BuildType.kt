package com.ruma.buildlogic.convention.logic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

internal fun configureAppBuildTypes(commonExtension: ApplicationExtension) {
    commonExtension.apply {
        buildTypes {
            getByName("debug") {
                applicationIdSuffix = ".debug"
                isDebuggable = true
                isMinifyEnabled = false
                isShrinkResources = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }

            getByName("release") {
                isDebuggable = false
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }
        }
    }
}

fun configureLibraryBuildTypes(commonExtension: LibraryExtension) {
    commonExtension.apply {
        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }
            getByName("release") {
                isMinifyEnabled = false // TODO: Change to true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            }
        }
    }
}
