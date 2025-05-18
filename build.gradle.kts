
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // efine Firebase plugin classpath
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}
