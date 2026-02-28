import com.android.build.api.dsl.LibraryExtension
import com.ruma.buildlogic.convention.logic.configureKotlinAndroid
import com.ruma.buildlogic.convention.logic.configureLibraryBuildTypes
import com.ruma.buildlogic.convention.logic.constants.IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.getTargetSdk
import com.ruma.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("com.ruma.repnote.buildlogic.convention.jvm-test")
                apply("com.ruma.repnote.buildlogic.convention.android-test")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                configureLibraryBuildTypes(this)
                testOptions {
                    targetSdk = getTargetSdk()
                    unitTests {
                        isIncludeAndroidResources = true
                        isReturnDefaultValues = true
                    }
                }
                lint {
                    targetSdk = getTargetSdk()
                }
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
            }

            dependencies {
                add(IMPLEMENTATION, libs.findLibrary("androidx-core-ktx").get())
                add(IMPLEMENTATION, libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
                add(
                    IMPLEMENTATION,
                    libs.findLibrary("androidx-lifecycle-viewmodel-navigation").get(),
                )
            }
        }
    }
}
