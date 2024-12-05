plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.novacodestudios.liminal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.novacodestudios.liminal"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        /*debug {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }*/

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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    // Material
    val materialVersion = "1.7.5"
    // implementation("androidx.compose.material3:material3-window-size-class:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:$materialVersion")
    implementation("androidx.compose.animation:animation:$materialVersion")
    implementation("androidx.compose.material:material:$materialVersion")
    implementation("androidx.compose.foundation:foundation:$materialVersion")

// Compose dependencies
    val lifeCycleVersion = "2.8.4"
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifeCycleVersion")

    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    val coroutinesVersion = "1.8.1"
// Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")


// Coroutine Lifecycle Scopes
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycleVersion")

    //Glide
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    //skrapeit (web scraping)
    //implementation("it.skrape:skrapeit:1.2.2")

    //Coil compose
    implementation("io.coil-kt:coil-compose:2.6.0")

    val hiltVersion = "2.49"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //implementation (libs.androidx.datastore.preferences)


    val roomKtxVersion = "2.6.1"
    implementation("androidx.room:room-ktx:$roomKtxVersion")
    implementation("androidx.room:room-runtime:$roomKtxVersion")
    annotationProcessor("androidx.room:room-compiler:$roomKtxVersion")
    ksp("androidx.room:room-compiler:$roomKtxVersion")
    implementation("androidx.room:room-paging:$roomKtxVersion")

    implementation("net.engawapg.lib:zoomable:1.6.1")

    // implementation("io.github.reactivecircus.cache4k:cache4k:0.13.0")
    //implementation("androidx.datastore:datastore-preferences:1.1.1")

    val pagingVersion = "3.3.0"

    implementation("androidx.paging:paging-common-ktx:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.17.2")

    // implementation("com.github.khushpanchal:Ketch:2.0.2")

    //workmanager
    implementation("androidx.hilt:hilt-work:1.2.0")
    val work_version = "2.9.1"
    implementation("androidx.work:work-runtime-ktx:$work_version")
    //work manager hilt
    implementation("androidx.work:work-runtime-ktx:$work_version")

}