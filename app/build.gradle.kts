plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.jiffydeliveryclient"
    compileSdk = 34
    packagingOptions{
        exclude("META-INF/DEPENDENCIES")
    }

    defaultConfig {
        applicationId = "com.example.jiffydeliveryclient"
        minSdk = 27
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.2")
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.2")
    implementation("com.google.android.libraries.places:places:4.0.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation("com.google.firebase:firebase-messaging:24.0.2")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //rx Java
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation ("io.reactivex.rxjava3:rxjava:3.1.5")

    //firebase authentication
    implementation ("com.firebaseui:firebase-ui-auth:8.0.2")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")

    //location

    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.gms:play-services-maps:19.0.0")

    //dexter
    implementation ("com.karumi:dexter:6.2.3")

    //GeoFire
    implementation ("com.firebase:geofire-android:3.2.0")
    implementation ("com.firebase:geofire-android-common:3.2.0")
    //Sliding Up
    implementation ("com.sothree.slidinguppanel:library:3.4.0")

    //retrofit
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:retrofit:r_latest_version")
    implementation ("com.squareup.retrofit2:converter-gson:2.1.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.1.0")
    implementation ("com.github.akarnokd:rxjava3-retrofit-adapter:3.0.0")
    //Event Bus
    implementation("org.greenrobot:eventbus:3.3.1")
    //Maps Utils
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")
    //Circle Image
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    //Picasso
    implementation ("com.squareup.picasso:picasso:2.8")

    //circular progress bar
    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")

    //Circle image view
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

}