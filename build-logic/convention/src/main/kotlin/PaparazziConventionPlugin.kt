import com.ruma.buildlogic.convention.logic.constants.TEST_IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class PaparazziConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("app.cash.paparazzi")
            dependencies {
                TEST_IMPLEMENTATION(libs.findLibrary("paparazzi").get())
            }
        }
    }
}
