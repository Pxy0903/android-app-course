plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.pengxiangyuan.lab4_cardgame"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.pengxiangyuan.lab4_cardgame"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 所有页面统一用 ViewBinding，不再使用 findViewById
    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
