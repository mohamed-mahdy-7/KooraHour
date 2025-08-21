// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false

    id ("com.google.dagger.hilt.android") version "2.57" apply false
    id ("com.google.devtools.ksp") version "2.1.21-2.0.1" apply false
    id("androidx.room") version "2.7.2" apply false
    alias(libs.plugins.google.gms.google.services) apply false

}