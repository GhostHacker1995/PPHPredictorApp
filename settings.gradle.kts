pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*") //required for Firebase
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal() // also needed for plugin resolution
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PPHPredictorApp"
include(":app")
