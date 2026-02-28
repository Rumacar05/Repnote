import com.ruma.buildlogic.convention.logic.constants.TEST_IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.constants.TEST_RUNTIME_ONLY
import com.ruma.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class JvmTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            tasks.withType<Test> {
                useJUnitPlatform()
            }

            dependencies {
                TEST_IMPLEMENTATION(libs.findLibrary("junit.jupiter.api").get())
                TEST_IMPLEMENTATION(libs.findLibrary("junit.jupiter.params").get())
                TEST_RUNTIME_ONLY(libs.findLibrary("junit.jupiter.engine").get())
                TEST_RUNTIME_ONLY(libs.findLibrary("junit.jupiter.vintage.engine").get())
                TEST_RUNTIME_ONLY(libs.findLibrary("junit.jupiter.launcher").get())
                TEST_IMPLEMENTATION(libs.findLibrary("mockk").get())
                TEST_IMPLEMENTATION(libs.findLibrary("kluent").get())
                TEST_IMPLEMENTATION(libs.findLibrary("kotest").get())
                TEST_IMPLEMENTATION(libs.findLibrary("kotest.assertions.table").get())
                TEST_IMPLEMENTATION(libs.findLibrary("turbine").get())
                TEST_IMPLEMENTATION(libs.findLibrary("kotlinx.coroutines.test").get())
            }
        }
    }
}
