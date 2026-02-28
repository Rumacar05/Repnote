import com.ruma.buildlogic.convention.logic.constants.IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.constants.KSP
import com.ruma.buildlogic.convention.logic.constants.TEST_IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JvmKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                add(IMPLEMENTATION, platform(libs.findLibrary("koin-bom").get()))
                add(IMPLEMENTATION, libs.findLibrary("koin-core").get())
                add(IMPLEMENTATION, libs.findLibrary("koin-annotations").get())
                add(KSP, libs.findLibrary("koin-ksp-compiler").get())

                // Testing
                add(TEST_IMPLEMENTATION, platform(libs.findLibrary("koin-bom").get()))
                add(TEST_IMPLEMENTATION, libs.findLibrary("koin-test").get())
                add(TEST_IMPLEMENTATION, libs.findLibrary("koin-test-junit5").get())
            }
        }
    }
}