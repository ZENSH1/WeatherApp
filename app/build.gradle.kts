import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.screenshot)
}

// The weatherapi.com key is read from local.properties (git-ignored, never commit it):
//     WEATHER_API_KEY=your_key
// It is only used by the `prod` flavor. The `mock` flavor works without it.
val localProperties = Properties().apply {
    providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText.orNull
        ?.let { load(it.reader()) }
}
val weatherApiKey: String = localProperties.getProperty("WEATHER_API_KEY")
    ?: providers.gradleProperty("WEATHER_API_KEY").orNull
    ?: ""

android {
    namespace = "com.example.weathertracking"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.weathertracking"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Two backends behind the same Model. Pick one with the "Build Variants" panel.
    //  - mock: canned, offline data (default). Great for learning and for UI work.
    //  - prod: the real weatherapi.com API (needs WEATHER_API_KEY).
    flavorDimensions += "backend"
    productFlavors {
        create("mock") {
            dimension = "backend"
            isDefault = true
        }
        create("prod") {
            dimension = "backend"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // Lets Robolectric-backed Compose UI tests in src/test read app resources.
        unitTests.isIncludeAndroidResources = true
    }

    // Screenshot tests: previews in src/screenshotTest, rendered on the JVM (see README).
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

androidComponents {
    onVariants { variant ->
        variant.buildConfigFields?.put(
            "WEATHER_API_KEY",
            BuildConfigField("String", "\"$weatherApiKey\"", "weatherapi.com API key (prod flavor)"),
        )
    }
}

room {
    // Exported schemas are the input for future Room migrations; commit them.
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Local persistence (part of the Model)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Images (weather condition icons)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.timber)

    // Real network backend, only compiled into the `prod` flavor.
    "prodImplementation"(libs.retrofit)
    "prodImplementation"(libs.retrofit.kotlinx.serialization)
    "prodImplementation"(libs.okhttp)
    "prodImplementation"(libs.kotlinx.serialization.json)

    // Local (JVM) tests: Model, Controller, Repository and Robolectric-backed View tests.
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)

    // Screenshot tests (Compose Preview Screenshot Testing)
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
    // prod-only unit tests: the Retrofit data source against a local fake HTTP server.
    "testProdImplementation"(libs.okhttp.mockwebserver)

    // Instrumented tests: Room DAO against a real SQLite engine.
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
}
