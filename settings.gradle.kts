pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven("https://jitpack.io") {
            name = "JitPack" // Optional: Give the repository a descriptive name
        }
    }
}

rootProject.name = "Liminal"
include(":app")
