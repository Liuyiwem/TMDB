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

rootProject.name = "TMDB"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core:common")
include(":core:network")
include(":core:data")
include(":core:data-test")
include(":core:model")
include(":core:testing")
include(":core:ui")
include(":domain")
include(":core:navigation")
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:search:api")
include(":feature:search:impl")
include(":feature:favorite:impl")
include(":feature:favorite:api")
