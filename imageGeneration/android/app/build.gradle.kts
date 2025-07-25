plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "antsearth.com.imagegenerator"
    compileSdk = 34

    defaultConfig {
        applicationId = "antsearth.com.imagegenerator"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    //implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3-android:1.2.1")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.functions)
    // Retrofit for making HTTP requests
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    // OkHttp for handling HTTP interactions
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    //image loading library
    implementation("io.coil-kt:coil-compose:1.3.2")

    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")

    //Camera related libraries
    implementation("io.coil-kt:coil-compose:1.4.0")
    implementation(libs.androidx.media3.exoplayer)
    //implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.ui)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    //implementation ("com.google.accompanist:accompanist-permissions:0.31.1-alpha")
    implementation ("com.google.accompanist:accompanist-coil:0.15.0")


    //Google Ads library
    implementation ("com.google.android.gms:play-services-ads:23.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}