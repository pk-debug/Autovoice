/*
 * Top-level build.gradle.kts
 * -----------------------------------------------------------------------------
 * We only DECLARE plugin versions here (`apply false`) using the version
 * catalog aliases — the actual `apply` happens inside each module's own
 * build.gradle.kts. This is the modern (AGP 8+) replacement for the old
 * `buildscript { dependencies { classpath(...) } }` pattern and avoids
 * duplicate-classpath resolution errors between modules.
 * -----------------------------------------------------------------------------
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinAndroidPlugin) apply false
    alias(libs.plugins.kotlinComposePlugin) apply false
    alias(libs.plugins.kspPlugin) apply false
    alias(libs.plugins.hiltPlugin) apply false
}
