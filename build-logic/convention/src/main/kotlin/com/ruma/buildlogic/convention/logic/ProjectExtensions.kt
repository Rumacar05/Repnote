package com.ruma.buildlogic.convention.logic

import com.ruma.buildlogic.convention.logic.constants.COMPILE_SDK
import com.ruma.buildlogic.convention.logic.constants.JAVA_VERSION
import com.ruma.buildlogic.convention.logic.constants.MIN_SDK
import com.ruma.buildlogic.convention.logic.constants.TARGET_SDK
import com.ruma.buildlogic.convention.logic.constants.VERSION_CODE
import com.ruma.buildlogic.convention.logic.constants.VERSION_NAME
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs
    get(): VersionCatalog =
        extensions
            .getByType<VersionCatalogsExtension>()
            .named("libs")

fun Project.getJavaVersion() =
    this.libs
        .findVersion(JAVA_VERSION)
        .get()
        .toString()
        .toInt()

fun Project.getCompileSdk() =
    this.libs
        .findVersion(COMPILE_SDK)
        .get()
        .toString()
        .toInt()

fun Project.getMinSdk() =
    this.libs
        .findVersion(MIN_SDK)
        .get()
        .toString()
        .toInt()

fun Project.getTargetSdk() =
    this.libs
        .findVersion(TARGET_SDK)
        .get()
        .toString()
        .toInt()

fun Project.getVersionCode() =
    this.libs
        .findVersion(VERSION_CODE)
        .get()
        .toString()
        .toInt()

fun Project.getVersionName() =
    this.libs
        .findVersion(VERSION_NAME)
        .get()
        .toString()
