plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "1.9.22"
    alias(libs.plugins.kotlin.compose) // if you use the Kotlin Compose plugin (recommended)
}

android {
    namespace = "tn.esprit.dam"
    compileSdk = 36

    defaultConfig {
        applicationId = "tn.esprit.dam"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // Modern AGP/Compose expect Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // If you are NOT using the kotlin.compose plugin, uncomment this and set the version:
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.15"
    // }
}

dependencies {

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Kotlin Serialization Converter for Retrofit
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Kotlinx Serialization runtime
    implementation("androidx.datastore:datastore-preferences:1.0.0") // DataStore for local JWT storage
// Keep all Compose artifacts aligned with a single BOM
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))

    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.animation:animation:1.6.8")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")          // <-- KeyboardOptions lives here
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Material 3
    implementation("androidx.compose.material3:material3")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.media3.decoder)

    // Testing / Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
