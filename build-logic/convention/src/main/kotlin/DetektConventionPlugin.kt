import com.ruma.buildlogic.convention.logic.getJavaVersion
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            subprojects.forEach { subproject ->
                subproject.plugins.apply("io.gitlab.arturbosch.detekt")
            }
            plugins.withId("io.gitlab.arturbosch.detekt") {
                extensions.configure<DetektExtension> {
                    val filesProp = project.findProperty("detektFiles") as String?
                    if (!filesProp.isNullOrBlank()) {
                        val fileList =
                            filesProp.split(",").filter { it.isNotBlank() }.map { file(it) }
                        source.setFrom(fileList)
                    } else {
                        source.setFrom("src/main/kotlin")
                    }
                }

                tasks.withType<Detekt>().configureEach {
                    jvmTarget = getJavaVersion().toString()
                    exclude("**/resources/**", "**/tmp/**", "**/build/**")
                }

                tasks.withType<DetektCreateBaselineTask>().configureEach {
                    jvmTarget = getJavaVersion().toString()
                }
            }
        }
    }
}
