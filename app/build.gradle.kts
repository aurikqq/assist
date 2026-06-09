plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.identity.jvm) // Add the dependency here
    }
}

android {
    namespace = "com.aurikqq.assistbasic"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aurikqq.assistbasic"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.2"

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    sourceSets {
        getByName("main") {
            assets.srcDirs(files("$buildDir/generated/assets"))
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
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(group = "com.alphacephei", name = "vosk-android", version = "0.3.75")
    implementation(libs.identity.jvm)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.media)
    implementation(libs.androidx.navigation.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("genUUID") {
    val generatedAssetsDir = project.layout.buildDirectory.dir("generated/assets/vosk-model-small-ru-0.22")
    val outputFileProvider = generatedAssetsDir.map { it.file("uuid") }

    outputs.file(outputFileProvider)
    doLast {
        val outputDirFile = generatedAssetsDir.get().asFile
        val outputFile = outputFileProvider.get().asFile
        outputDirFile.mkdirs()
        outputFile.writeText(com.android.identity.util.UUID.randomUUID().toString())
    }
}


tasks.named("preBuild") {
    dependsOn("genUUID")
}