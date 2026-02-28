import com.ruma.buildlogic.convention.logic.constants.IMPLEMENTATION
import com.ruma.buildlogic.convention.logic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.ruma.repnote.buildlogic.convention.android-library-compose")
                apply("com.ruma.repnote.buildlogic.convention.android-koin")
                apply("com.ruma.repnote.buildlogic.convention.android-test")
            }

            dependencies {
                add(IMPLEMENTATION, project(":core:design-system"))
                add(IMPLEMENTATION, project(":core:string-resources"))

                add(IMPLEMENTATION, libs.findLibrary("koin-androidx-compose").get())
                add(IMPLEMENTATION, libs.findLibrary("koin-androidx-compose-navigation").get())
            }
        }
    }
}
