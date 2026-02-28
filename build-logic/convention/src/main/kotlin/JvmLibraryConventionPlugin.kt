import com.ruma.buildlogic.convention.logic.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("com.ruma.repnote.buildlogic.convention.jvm-test")
            }
            configureKotlinJvm()
        }
    }
}
