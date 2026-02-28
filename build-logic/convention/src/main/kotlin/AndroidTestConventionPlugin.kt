import com.ruma.buildlogic.convention.logic.constants.ANDROID_TEST_IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.constants.TEST_IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.constants.TEST_RUNTIME_ONLY
import com.ruma.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                TEST_IMPLEMENTATION(libs.findLibrary("junit").get())
                TEST_IMPLEMENTATION(libs.findLibrary("androidx.compose.ui.test.junit4").get())
                TEST_RUNTIME_ONLY(libs.findLibrary("junit-jupiter-launcher").get())
                ANDROID_TEST_IMPLEMENTATION(libs.findLibrary("androidx.junit").get())
                ANDROID_TEST_IMPLEMENTATION(libs.findLibrary("androidx.espresso.core").get())
                ANDROID_TEST_IMPLEMENTATION(
                    libs.findLibrary("androidx.compose.ui.test.junit4").get(),
                )
            }
        }
    }
}
