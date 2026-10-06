pluginManagement {
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
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "AksharDyslexiaReader"
include(":app")

// Direct build directory to local cache outside OneDrive sync to eliminate Windows file locks
gradle.beforeProject {
    val localBuildDir = file("C:/Users/jawadwar priti/.gradle-build/Akshar/${this.name}")
    this.layout.buildDirectory.set(localBuildDir)
}
