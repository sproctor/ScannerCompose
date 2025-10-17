pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    // See https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.60.5"
////                            # available:"0.60.6"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
////                                                   # available:"0.10.0"
////                                                   # available:"1.0.0-rc-1"
////                                                   # available:"1.0.0"
}

rootProject.name = "ScannerCompose"
include(":app")
include(":zebrascannerandroid")
