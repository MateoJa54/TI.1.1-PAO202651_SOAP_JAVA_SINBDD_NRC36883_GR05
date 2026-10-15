plugins {
    id("com.android.application") version "9.4.1"
}

android {
    namespace = "ec.edu.monster"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ec.edu.monster.climov"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
