plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

/**
 * Version code, derived from the number of commits.
 *
 * Android only treats an APK as an upgrade when its versionCode is higher than the installed
 * one, so a hardcoded value means no build can ever update another. Commit count rises on every
 * commit with no manual step and no state to keep in sync.
 *
 * CI must check out with `fetch-depth: 0`. A shallow clone reports a smaller count, which would
 * make the version go *backwards* — and Android refuses to install that outright.
 *
 * Override with `-PversionCode=N` when a build has to be pinned.
 */
val gitVersionCode: Int =
  (project.findProperty("versionCode") as String?)?.toIntOrNull()
    ?: runCatching {
        // providers.exec is the configuration-cache-safe way to shell out; this project has
        // org.gradle.configuration-cache enabled.
        providers
          .exec { commandLine("git", "rev-list", "--count", "HEAD") }
          .standardOutput
          .asText
          .get()
          .trim()
          .toInt()
      }
      .getOrNull()
      ?.takeIf { it > 0 } ?: 1

val appVersionName = "1.0.$gitVersionCode"

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    // Permanent identity of the app. Changing it makes Android treat the result as a different
    // app rather than an update, so this must not move again.
    applicationId = "com.fixhora.app"
    minSdk = 24
    targetSdk = 36
    versionCode = gitVersionCode
    versionName = appVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Distributed APKs are signed by CI with a key held in GitHub Secrets. Every release build
  // shares that one key, which is what allows each new APK to install over the previous one:
  // Android rejects an update signed with a different key.
  val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
  val hasReleaseKeystore = !keystorePath.isNullOrBlank() && file(keystorePath).exists()

  // An unsigned APK cannot be installed on any device, so quietly producing one is worse than
  // stopping. This only trips when a release task was actually requested, leaving `assembleDebug`
  // and `test` working on a clean clone with no secrets. startParameter is read at configuration
  // time and is configuration-cache safe, unlike gradle.taskGraph.
  val releaseRequested =
    gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
  if (releaseRequested && !hasReleaseKeystore) {
    throw GradleException(
      """
      Cannot build a release APK: no signing keystore.

      RELEASE_KEYSTORE_PATH is ${if (keystorePath.isNullOrBlank()) "not set" else "set to '$keystorePath', which does not exist"}.

      Release APKs must be signed with the project's release key, or they cannot be installed at
      all, and an APK signed with any other key cannot update one already on a device.

      In CI this comes from the RELEASE_KEYSTORE_BASE64 secret. Locally, run
      ./scripts/make-release-keystore.sh and then export RELEASE_KEYSTORE_PATH,
      RELEASE_KEYSTORE_PASSWORD, RELEASE_KEY_ALIAS and RELEASE_KEY_PASSWORD.

      To just try the app instead, use: ./gradlew assembleDebug
      """
        .trimIndent()
    )
  }

  signingConfigs {
    // Checked into the repo on purpose. AGP's default is the *machine-local*
    // ~/.android/debug.keystore, so a debug APK built here could never update one built on your
    // laptop or in CI. A shared debug key makes the debug channel updatable everywhere. It is not
    // a secret: Android's own default debug key is public, and Play Store rejects debug-signed
    // builds regardless.
    getByName("debug") {
      storeFile = rootProject.file("keystore/fixhora-debug.jks")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }

    if (hasReleaseKeystore) {
      create("release") {
        storeFile = file(keystorePath!!)
        storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
        keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: "fixhora"
        keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release")
      // app_name lives here rather than in strings.xml so each channel can label itself; a
      // resValue would otherwise collide with a same-named string resource.
      resValue("string", "app_name", "FixoraX")
    }
    debug {
      // Debug builds are signed with the machine-local debug keystore, so they can never update a
      // CI-signed release APK. Giving them their own id lets both live on one device instead of
      // the install failing with INSTALL_FAILED_UPDATE_INCOMPATIBLE.
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
      resValue("string", "app_name", "FixoraX (Debug)")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Camera dependencies stay commented out until in-app capture is actually built; they are still
// declared in the version catalog so re-enabling them is a one-line change.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.play.services.location)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}
