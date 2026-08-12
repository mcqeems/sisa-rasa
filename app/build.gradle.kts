import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// ponytail: google-services needs google-services.json; apply only when present so
// the project builds cleanly without Firebase config (secrets.properties FIREBASE_ENABLED=false).
// Declared `apply false` in root build.gradle.kts to keep it on the build classpath.
// NB: use a projectDir-relative path. A bare relative File() resolves against the Gradle
// daemon's CWD (~/.gradle/daemon/...) and silently misses app/google-services.json.
if (File(projectDir, "google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsFile.exists()) secretsFile.inputStream().use { load(it) }
}
fun secret(key: String, fallback: String): String = secrets.getProperty(key) ?: fallback

android {
    namespace = "com.mobile.sisarasa"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mobile.sisarasa"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("boolean", "FIREBASE_ENABLED", secret("FIREBASE_ENABLED", "false"))
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"${secret("FIREBASE_DATABASE_URL", "https://sisarasa-d93f2-default-rtdb.asia-southeast1.firebasedatabase.app/")}\"")
        buildConfigField("String", "MAPS_API_KEY", "\"${secret("MAPS_API_KEY", "")}\"")
        manifestPlaceholders["MAPS_API_KEY"] = secret("MAPS_API_KEY", "")
        buildConfigField("String", "SUPABASE_URL", "\"${secret("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${secret("SUPABASE_ANON_KEY", "")}\"")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.play.services.maps)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.supabase.storage)

    // ponytail: Firebase compiles without config; risky only if FIREBASE_ENABLED=true with no JSON.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}