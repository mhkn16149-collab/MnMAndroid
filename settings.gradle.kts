
pluginManagement {
    repositories {
        google()
        maven {
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        gradlePluginPortal()
    }

    plugins {
        id("com.android.application") version "8.7.3"
        id("org.jetbrains.kotlin.android") version "2.0.21"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
    }
}

rootProject.name = "MnM"
include(":app")
