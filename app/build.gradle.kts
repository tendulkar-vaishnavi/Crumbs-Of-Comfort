plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.crumbsofcomfort"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.crumbsofcomfort"
        minSdk = 24
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures{
        viewBinding=true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.play.services.auth)
    implementation (libs.firebase.auth.v2231)
    implementation (libs.checkout.v1640)
    implementation (libs.checkout)
    implementation (libs.cloudinary.android.v220)
    implementation (libs.lottie)
    //implementation (libs.play.services.location)

    implementation (libs.okhttp)


    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    implementation (libs.play.services.location.v2101)


    implementation (libs.material)
    implementation (libs.materialdatetimepicker)

    implementation (libs.firebase.database.v2030)
    implementation (libs.firebase.core)
    implementation(libs.firebase.auth.v2106)
    implementation (libs.coordinatorlayout)
    implementation(libs.glide)
    implementation(libs.gson)
    implementation(libs.dotsindicator)
}