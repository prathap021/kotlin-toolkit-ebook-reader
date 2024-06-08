/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.parcelize")
    alias(libs.plugins.ksp)
}

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 34

        applicationId = "org.readium.r2reader"

        versionName = "3.0.0-alpha.2"
        versionCode = 300000

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk.abiFilters.add("armeabi-v7a")
        ndk.abiFilters.add("arm64-v8a")
        ndk.abiFilters.add("x86")
        ndk.abiFilters.add("x86_64")
        vectorDrawables {
            useSupportLibrary = true
        }


    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"))
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        resources.excludes.add("META-INF/*")
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            res.srcDirs("src/main/res")
            assets.srcDirs("src/main/assets")
        }
    }
    namespace = "org.readium.r2.testapp"
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)


    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.legacy.v4)

    implementation(project(":readium:readium-shared"))
    implementation(project(":readium:readium-streamer"))
    implementation(project(":readium:readium-navigator"))
    implementation(project(":readium:navigators:media:readium-navigator-media-audio"))
    implementation(project(":readium:navigators:media:readium-navigator-media-tts"))
    // Only required if you want to support audiobooks using ExoPlayer.
    implementation(project(":readium:adapters:exoplayer"))
    implementation(project(":readium:readium-navigator-media2"))
    implementation(project(":readium:readium-opds"))
    implementation(project(":readium:readium-lcp"))
    // Only required if you want to support PDF files using PDFium.
    implementation(project(":readium:adapters:pdfium"))


    implementation( "com.squareup.retrofit2:retrofit:2.9.0")
    implementation( "com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

// Retrofit with Kotlin serialization Converter

    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")


    implementation("androidx.compose.material:material-icons-extended:1.6.4")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")

    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.cardview)

    implementation(libs.bundles.compose)
//    debugImplementation(libs.androidx.compose.ui)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.paging)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.webkit)
    implementation(libs.google.material)
    implementation(libs.timber)
    implementation(libs.picasso)
    implementation(libs.joda.time)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.jsoup)

    implementation(libs.bundles.media3)

    // Room database
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    implementation("androidx.datastore:datastore-preferences:1.0.0")

//    implementation

    
    implementation( "io.coil-kt:coil-compose:2.5.0")
    implementation( "io.coil-kt:coil-gif:2.1.0")
    implementation("com.airbnb.android:lottie-compose:4.0.0")



    // Tests
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.ext.junit)
    androidTestImplementation(libs.androidx.expresso.core)


}
