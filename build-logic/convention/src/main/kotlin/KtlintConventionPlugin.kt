import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

class KtlintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")
            extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
                android.set(true)
                reporters {
                    reporter(ReporterType.PLAIN)
                    reporter(ReporterType.HTML)
                }
                filter {
                    exclude("**/generated/**")
                }
            }
        }
    }
}
