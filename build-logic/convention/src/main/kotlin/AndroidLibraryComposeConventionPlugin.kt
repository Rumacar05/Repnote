import com.android.build.api.dsl.LibraryExtension
import com.ruma.buildlogic.convention.logic.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.ruma.repnote.buildlogic.convention.android-library")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this)
            }
        }
    }
}
