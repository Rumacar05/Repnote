pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "Repnote"
include(":app")

include(":core:auth:api")
include(":core:auth:impl")
include(":core:data")
include(":core:database")
include(":core:design-system")
include(":core:domain")
include(":core:string-resources")

include(":feature:auth:api")
include(":feature:auth:impl")
include(":feature:home:impl")
include(":feature:profile:impl")
include(":feature:routine:impl")
include(":feature:workout:impl")
