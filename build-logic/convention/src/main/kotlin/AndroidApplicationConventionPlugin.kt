import com.android.build.api.dsl.ApplicationExtension
import com.ruma.buildlogic.convention.logic.configureAndroidCompose
import com.ruma.buildlogic.convention.logic.configureAppBuildTypes
import com.ruma.buildlogic.convention.logic.configureKotlinAndroid
import com.ruma.buildlogic.convention.logic.getTargetSdk
import com.ruma.buildlogic.convention.logic.getVersionCode
import com.ruma.buildlogic.convention.logic.getVersionName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("com.ruma.repnote.buildlogic.convention.jvm-test")
                apply("com.ruma.repnote.buildlogic.convention.android-test")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureAppBuildTypes(this)
                configureAndroidCompose(this)
                defaultConfig {
                    targetSdk = getTargetSdk()
                    versionCode = getVersionCode()
                    versionName = getVersionName()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                    }
                }
            }
        }
    }
}
