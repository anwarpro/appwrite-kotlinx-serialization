plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization")
}

android {
    namespace = "io.appwrite.appwrite"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
//    api(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
//    api("com.squareup.okhttp3:okhttp")
//    implementation("com.squareup.okhttp3:okhttp-urlconnection")
//    implementation("com.squareup.okhttp3:logging-interceptor")
//    implementation("com.google.code.gson:gson:2.9.0")

    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-okhttp:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
//    implementation("io.ktor:ktor-serialization-gson:2.3.3")
    api("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    implementation("io.ktor:ktor-client-websockets:2.3.3")
//    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-client-logging:2.3.3")
//    implementation("com.russhwolf:multiplatform-settings:1.1.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.5.1")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.browser:browser:1.4.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}