/*
 * settings.gradle.kts
 * -----------------------------------------------------------------------------
 * Declares the two Gradle modules that make up AutoVoice OS:
 *
 *   :voice-sdk  -> Android LIBRARY module. Framework-agnostic "engine" that
 *                  simulates a cloud Voice AI backend (STT text in, structured
 *                  intent JSON out). This is what you'd ship as a standalone
 *                  .aar to a partner OEM, so it must have ZERO knowledge of
 *                  Compose, Hilt, or any :app-specific code.
 *
 *   :app        -> Android APPLICATION module. The automotive head-unit UI
 *                  (Jetpack Compose + Car App Library) that CONSUMES the SDK.
 *
 * Because dependencyResolutionManagement below sets
 * repositoriesMode = FAIL_ON_PROJECT_REPOS, no module is allowed to declare
 * its own `repositories {}` block — this keeps supply-chain / repo trust
 * centralised in exactly one place, which is what most enterprise Android
 * teams enforce for security review.
 * -----------------------------------------------------------------------------
 */
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
    }
}

rootProject.name = "AutoVoiceOS"
include(":app")
include(":voice-sdk")



//pluginManagement {
//    repositories {
//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}
//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
//}
//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//    }
//}
//
//rootProject.name = "Autovoice"
//include(":app")
//include(":voice-sdk")
