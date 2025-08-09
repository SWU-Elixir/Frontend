import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
val properties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}