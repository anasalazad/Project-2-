#!/usr/bin/env python3
"""
Replays The Feline Co. as a series of small, buildable substages in a scratch git repo,
then exports one patch per substage. Hub files (Gradle, manifest, MainActivity, NavHost,
ViewModel factory) are generated per stage so every substage compiles on its own.
"""
import os, shutil, subprocess, json, sys, textwrap

SRC = "/home/user/Project-2-"
SCR = "/tmp/claude-0/-home-user-Project-2-/1bac1f28-0fd0-5431-af45-4c14601b7bc9/scratchpad"
PREFAV = f"{SCR}/prefav"
OUT = f"{SCR}/staged"
K = "app/src/main/java/com/thefelineco/"
T = "app/src/test/java/com/thefelineco/"
A = "app/src/androidTest/java/com/thefelineco/"
R = "app/src/main/res/"

# --------------------------------------------------------------------------------------------
# Gradle generation (feature-gated lines, in final-file order)
# --------------------------------------------------------------------------------------------
CATALOG_VERSIONS = [
    ("base", 'agp = "8.7.3"'), ("base", 'kotlin = "2.1.0"'), ("room", 'ksp = "2.1.0-1.0.29"'),
    ("base", 'coreKtx = "1.15.0"'), ("base", 'lifecycle = "2.8.7"'), ("base", 'activityCompose = "1.9.3"'),
    ("base", 'composeBom = "2024.12.01"'), ("navigation", 'navigation = "2.8.5"'), ("room", 'room = "2.6.1"'),
    ("datastore", 'datastore = "1.1.1"'), ("navigation", 'serialization = "1.7.3"'), ("coil", 'coil = "2.7.0"'),
    ("coroutines", 'coroutines = "1.9.0"'), ("unittest", 'junit = "4.13.2"'),
    ("androidtest", 'androidxJunit = "1.2.1"'), ("androidtest", 'espresso = "3.6.1"'),
]
CATALOG_LIBS = [
    ("base", 'androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }'),
    ("base", 'androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }'),
    ("lifecyclecompose", 'androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }'),
    ("lifecyclecompose", 'androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }'),
    ("base", 'androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }'),
    ("base", 'androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }'),
    ("base", 'androidx-ui = { group = "androidx.compose.ui", name = "ui" }'),
    ("base", 'androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }'),
    ("base", 'androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }'),
    ("base", 'androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }'),
    ("uitest", 'androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }'),
    ("uitest", 'androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }'),
    ("base", 'androidx-material3 = { group = "androidx.compose.material3", name = "material3" }'),
    ("navsuite", 'androidx-material3-adaptive-navigation-suite = { group = "androidx.compose.material3", name = "material3-adaptive-navigation-suite" }'),
    ("icons", 'androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }'),
    ("navigation", 'androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }'),
    ("room", 'androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }'),
    ("room", 'androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }'),
    ("room", 'androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }'),
    ("datastore", 'androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }'),
    ("navigation", 'kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }'),
    ("coroutines", 'kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }'),
    ("coil", 'coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }'),
    ("unittest", 'junit = { group = "junit", name = "junit", version.ref = "junit" }'),
    ("androidtest", 'androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxJunit" }'),
    ("androidtest", 'androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }'),
]
CATALOG_PLUGINS = [
    ("base", 'android-application = { id = "com.android.application", version.ref = "agp" }'),
    ("base", 'kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }'),
    ("base", 'kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }'),
    ("navigation", 'kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }'),
    ("parcelize", 'kotlin-parcelize = { id = "org.jetbrains.kotlin.plugin.parcelize", version.ref = "kotlin" }'),
    ("room", 'ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }'),
]
PLUGIN_ALIASES = [  # (feature, alias)
    ("base", "android.application"), ("base", "kotlin.android"), ("base", "kotlin.compose"),
    ("navigation", "kotlin.serialization"), ("parcelize", "kotlin.parcelize"), ("room", "ksp"),
]
APP_DEPS = [  # (feature, line) ; None line = blank separator owned by feature
    ("base", "    implementation(libs.androidx.core.ktx)"),
    ("base", "    implementation(libs.androidx.lifecycle.runtime.ktx)"),
    ("lifecyclecompose", "    implementation(libs.androidx.lifecycle.runtime.compose)"),
    ("lifecyclecompose", "    implementation(libs.androidx.lifecycle.viewmodel.compose)"),
    ("base", "    implementation(libs.androidx.activity.compose)"),
    ("base", ""),
    ("base", "    implementation(platform(libs.androidx.compose.bom))"),
    ("base", "    implementation(libs.androidx.ui)"),
    ("base", "    implementation(libs.androidx.ui.graphics)"),
    ("base", "    implementation(libs.androidx.ui.tooling.preview)"),
    ("base", "    implementation(libs.androidx.material3)"),
    ("navsuite", "    implementation(libs.androidx.material3.adaptive.navigation.suite)"),
    ("icons", "    implementation(libs.androidx.material.icons.extended)"),
    ("navigation", ""),
    ("navigation", "    implementation(libs.androidx.navigation.compose)"),
    ("navigation", "    implementation(libs.kotlinx.serialization.json)"),
    ("room", ""),
    ("room", "    implementation(libs.androidx.room.runtime)"),
    ("room", "    implementation(libs.androidx.room.ktx)"),
    ("room", "    ksp(libs.androidx.room.compiler)"),
    ("datastore|coil", ""),
    ("datastore", "    implementation(libs.androidx.datastore.preferences)"),
    ("coil", "    implementation(libs.coil.compose)"),
    ("unittest", ""),
    ("unittest", "    testImplementation(libs.junit)"),
    ("coroutines", "    testImplementation(libs.kotlinx.coroutines.test)"),
    ("androidtest", ""),
    ("androidtest", "    androidTestImplementation(libs.androidx.junit)"),
    ("androidtest", "    androidTestImplementation(libs.androidx.espresso.core)"),
    ("uitest", "    androidTestImplementation(platform(libs.androidx.compose.bom))"),
    ("uitest", "    androidTestImplementation(libs.androidx.ui.test.junit4)"),
    ("androidtest", "    androidTestImplementation(libs.kotlinx.coroutines.test)"),
    ("base", ""),
    ("base", "    debugImplementation(libs.androidx.ui.tooling)"),
    ("uitest", "    debugImplementation(libs.androidx.ui.test.manifest)"),
]


def on(tag, feats):
    return any(t in feats for t in tag.split("|"))


def gen_catalog(feats):
    feats = set(feats) | ({"coroutines"} if {"androidtest", "coroutinestest"} & set(feats) else set())
    out = ["[versions]"] + [l for t, l in CATALOG_VERSIONS if on(t, feats)]
    out += ["", "[libraries]"] + [l for t, l in CATALOG_LIBS if on(t, feats)]
    out += ["", "[plugins]"] + [l for t, l in CATALOG_PLUGINS if on(t, feats)]
    return "\n".join(out) + "\n"


def gen_root_build(feats):
    lines = ["// Top-level build file. Plugins are declared here and applied in the module build files.", "plugins {"]
    lines += [f"    alias(libs.plugins.{a}) apply false" for t, a in PLUGIN_ALIASES if on(t, feats)]
    return "\n".join(lines + ["}"]) + "\n"


def gen_app_build(feats):
    feats = set(feats)
    if "coroutinestest" in feats:
        feats.add("coroutines")
    plugins = "\n".join(f"    alias(libs.plugins.{a})" for t, a in PLUGIN_ALIASES if on(t, feats))
    deps, prev_blank = [], True
    for t, l in APP_DEPS:
        if not on(t, feats):
            continue
        if l == "":
            if not prev_blank:
                deps.append("")
            prev_blank = True
        else:
            deps.append(l)
            prev_blank = False
    while deps and deps[-1] == "":
        deps.pop()
    ksp_block = """
ksp {
    // Exports the Room schema so database changes can be reviewed in version control.
    arg("room.schemaLocation", "$projectDir/schemas")
}
""" if "room" in feats else ""
    return f"""import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {{
{plugins}
}}

android {{
    namespace = "com.thefelineco"
    compileSdk = 35

    defaultConfig {{
        applicationId = "com.thefelineco"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }}

    buildTypes {{
        release {{
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }}
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }}
    buildFeatures {{
        compose = true
    }}
}}

kotlin {{
    compilerOptions {{
        jvmTarget.set(JvmTarget.JVM_17)
    }}
}}
{ksp_block}
dependencies {{
""" + "\n".join(deps) + "\n}\n"


def gen_gradle_properties(feats):
    s = """# Project-wide Gradle settings.
# 2 GB suits an 8 GB laptop running Android Studio at the same time.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
"""
    if "room" in feats:
        s += """# Room 2.6.1 is built for KSP1; keep it explicit so a KSP upgrade doesn't silently switch to KSP2.
ksp.useKSP2=false
"""
    return s


GITIGNORE = """*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build/
/app/build/
/captures/
.externalNativeBuild/
.cxx/
.kotlin/
"""

# --------------------------------------------------------------------------------------------
# Manifest
# --------------------------------------------------------------------------------------------
def gen_manifest(level):
    name = '        android:name=".FelineApplication"\n' if level >= 2 else ""
    booking = """
        <!-- Opened from a cat's profile with a Parcelable Cat; returns a Parcelable Booking. -->
        <activity
            android:name=".ui.booking.BookingActivity"
            android:exported="false"
            android:windowSoftInputMode="adjustResize" />
""" if level >= 3 else ""
    checkout = """
        <!-- Opened from the basket with an ArrayList<CartItem>; returns a Parcelable Order. -->
        <activity
            android:name=".ui.checkout.CheckoutActivity"
            android:exported="false"
            android:windowSoftInputMode="adjustResize" />
""" if level >= 4 else ""
    return f"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
{name}        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.FelineCo">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
{booking}{checkout}
    </application>

</manifest>
"""

# --------------------------------------------------------------------------------------------
# MainActivity versions
# --------------------------------------------------------------------------------------------
MAIN_HELLO = """package com.thefelineco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Entry point. For now it only proves the Compose setup works. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("The Feline Co.", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
"""

MAIN_THEMED = """package com.thefelineco

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thefelineco.ui.theme.FelineTheme

/** Entry point. Shows a type and colour check in the brand theme. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The brand is dark-first, so the system bar icons are light.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            FelineTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "The Feline Co.",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
"""

MAIN_SPLASH = """package com.thefelineco

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.thefelineco.ui.common.SplashScreen
import com.thefelineco.ui.theme.FelineTheme

/** Entry point. Shows the branded splash until the rest of the app is built. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The brand is dark-first, so the system bar icons are light.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            FelineTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SplashScreen()
                }
            }
        }
    }
}
"""

MAIN_AUTH = """package com.thefelineco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thefelineco.di.AppViewModelProvider
import com.thefelineco.domain.model.User
import com.thefelineco.ui.MainViewModel
import com.thefelineco.ui.SessionState
import com.thefelineco.ui.auth.AuthNavHost
import com.thefelineco.ui.common.SplashScreen
import com.thefelineco.ui.common.setFelineContent

/**
 * Entry point. Shows the sign-in flow or the signed-in app depending on the saved session.
 * The signed-in side is a temporary placeholder until the navigation shell is built.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFelineContent {
            val session by viewModel.session.collectAsStateWithLifecycle()
            AnimatedContent(
                targetState = session,
                contentKey = { it.contentKey },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "session",
            ) { state ->
                when (state) {
                    SessionState.Loading -> SplashScreen()
                    SessionState.SignedOut -> AuthNavHost()
                    is SessionState.SignedIn -> SignedInPlaceholder(state.user, onLogout = viewModel::logout)
                }
            }
        }
    }
}

/** Temporary: proves sign-in works before the app shell exists. */
@Composable
private fun SignedInPlaceholder(user: User, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Signed in as ${user.fullName}", style = MaterialTheme.typography.headlineSmall)
        Text("${user.credits} credits · ${if (user.isAdmin) "Admin" else "Customer"}")
        Button(onClick = onLogout) { Text("Log out") }
    }
}
"""

# --------------------------------------------------------------------------------------------
# ViewModel factory (AppViewModelProvider)
# --------------------------------------------------------------------------------------------
VM_ENTRIES = {
    "main": (["com.thefelineco.ui.MainViewModel"], "        initializer { MainViewModel(container().userRepository, container().shopRepository) }"),
    "login": (["com.thefelineco.ui.auth.LoginViewModel"], "        initializer { LoginViewModel(container().userRepository) }"),
    "register": (["com.thefelineco.ui.auth.RegisterViewModel"], "        initializer { RegisterViewModel(container().userRepository) }"),
    "home": (["com.thefelineco.ui.home.HomeViewModel"], """        initializer {
            HomeViewModel(container().userRepository, container().catRepository, container().shopRepository)
        }"""),
    "adopt_v1": (["com.thefelineco.ui.adopt.AdoptViewModel", "com.thefelineco.ui.navigation.AdoptRoute", "ROUTE"], """        initializer {
            // Route arguments arrive through the SavedStateHandle of the navigation back stack entry.
            AdoptViewModel(createSavedStateHandle().toRoute<AdoptRoute>().freeOnly, container().catRepository)
        }"""),
    "adopt": (["com.thefelineco.ui.adopt.AdoptViewModel", "com.thefelineco.ui.navigation.AdoptRoute", "ROUTE"], """        initializer {
            // Route arguments arrive through the SavedStateHandle of the navigation back stack entry.
            AdoptViewModel(
                initialFreeOnly = createSavedStateHandle().toRoute<AdoptRoute>().freeOnly,
                catRepository = container().catRepository,
                userRepository = container().userRepository,
            )
        }"""),
    "detail": (["com.thefelineco.ui.catdetail.CatDetailViewModel", "com.thefelineco.ui.navigation.CatDetailRoute", "ROUTE"], """        initializer {
            CatDetailViewModel(
                catId = createSavedStateHandle().toRoute<CatDetailRoute>().catId,
                catRepository = container().catRepository,
                userRepository = container().userRepository,
            )
        }"""),
    "bookings": (["com.thefelineco.ui.bookings.BookingsViewModel"], "        initializer { BookingsViewModel(container().userRepository, container().bookingRepository) }"),
    "shop": (["com.thefelineco.ui.shop.ShopViewModel"], "        initializer { ShopViewModel(container().userRepository, container().shopRepository) }"),
    "basket": (["com.thefelineco.ui.basket.BasketViewModel"], "        initializer { BasketViewModel(container().userRepository, container().shopRepository) }"),
    "profile": (["com.thefelineco.ui.profile.ProfileViewModel"], """        initializer {
            ProfileViewModel(container().userRepository, container().shopRepository, container().settingsStore)
        }"""),
    "a_dash": (["com.thefelineco.ui.admin.AdminDashboardViewModel"], """
        // Admin
        initializer {
            AdminDashboardViewModel(
                container().userRepository, container().catRepository,
                container().bookingRepository, container().shopRepository,
            )
        }"""),
    "a_cats": (["com.thefelineco.ui.admin.AdminCatsViewModel"], "        initializer { AdminCatsViewModel(container().catRepository) }"),
    "a_catform": (["com.thefelineco.ui.admin.CatFormViewModel", "com.thefelineco.ui.navigation.AdminCatEditRoute", "ROUTE"],
                  "        initializer { CatFormViewModel(createSavedStateHandle().toRoute<AdminCatEditRoute>().catId, container().catRepository) }"),
    "a_bookings": (["com.thefelineco.ui.admin.AdminBookingsViewModel"], "        initializer { AdminBookingsViewModel(container().bookingRepository) }"),
    "a_products": (["com.thefelineco.ui.admin.AdminProductsViewModel"], "        initializer { AdminProductsViewModel(container().shopRepository) }"),
    "a_productform": (["com.thefelineco.ui.admin.ProductFormViewModel", "com.thefelineco.ui.navigation.AdminProductEditRoute", "ROUTE"], """        initializer {
            ProductFormViewModel(createSavedStateHandle().toRoute<AdminProductEditRoute>().productId, container().shopRepository)
        }"""),
}
VM_ORDER = ["main", "login", "register", "home", "adopt_v1", "adopt", "detail", "bookings", "shop", "basket", "profile",
            "a_dash", "a_cats", "a_catform", "a_bookings", "a_products", "a_productform"]


def gen_vmp(entries):
    imports = {"androidx.lifecycle.ViewModelProvider", "androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY",
               "androidx.lifecycle.viewmodel.CreationExtras", "androidx.lifecycle.viewmodel.initializer",
               "androidx.lifecycle.viewmodel.viewModelFactory", "com.thefelineco.FelineApplication"}
    body = []
    for key in VM_ORDER:
        if key in entries:
            imps, block = VM_ENTRIES[key]
            for i in imps:
                if i == "ROUTE":
                    imports |= {"androidx.lifecycle.createSavedStateHandle", "androidx.navigation.toRoute"}
                else:
                    imports.add(i)
            body.append(block)
    imp = "\n".join(f"import {i}" for i in sorted(imports))
    return f"""package com.thefelineco.di

{imp}

/**
 * Creates every ViewModel with its dependencies from [AppContainer].
 * Usage: `viewModel(factory = AppViewModelProvider.Factory)`.
 */
object AppViewModelProvider {{
    val Factory: ViewModelProvider.Factory = viewModelFactory {{
{chr(10).join(body)}
    }}
}}

/** The app's [AppContainer], taken from the Application object inside the ViewModel's creation extras. */
fun CreationExtras.container(): AppContainer = (this[APPLICATION_KEY] as FelineApplication).container
"""

# --------------------------------------------------------------------------------------------
# NavHost
# --------------------------------------------------------------------------------------------
ROUTES = ["home", "adopt", "detail", "shop", "basket", "bookings", "profile",
          "a_dash", "a_cats", "a_catedit", "a_bookings", "a_products", "a_productedit"]
ROUTE_CLASS = {"home": "HomeRoute", "adopt": "AdoptRoute", "detail": "CatDetailRoute", "shop": "ShopRoute",
               "basket": "BasketRoute", "bookings": "BookingsRoute", "profile": "ProfileRoute",
               "a_dash": "AdminDashboardRoute", "a_cats": "AdminCatsRoute", "a_catedit": "AdminCatEditRoute",
               "a_bookings": "AdminBookingsRoute", "a_products": "AdminProductsRoute", "a_productedit": "AdminProductEditRoute"}
PLACEHOLDER_TITLE = {"home": "Home", "adopt": "Adopt", "detail": "Cat profile", "shop": "Shop", "basket": "Basket",
                     "bookings": "My bookings", "a_dash": "Admin dashboard", "a_cats": "Manage cats", "a_catedit": "Edit cat",
                     "a_bookings": "Appointments", "a_products": "Manage products", "a_productedit": "Edit product"}

NAV_BLOCKS = {
    "home": (["com.thefelineco.ui.home.HomeScreen"], """        composable<HomeRoute> {
            HomeScreen(
                onOpenCat = { catId -> navController.navigate(CatDetailRoute(catId)) },
                onBrowseCats = { freeOnly -> navController.navigate(AdoptRoute(freeOnly)) },
                onOpenShop = { navController.navigateToTopLevel(ShopRoute) },
            )
        }"""),
    "adopt": (["com.thefelineco.ui.adopt.AdoptScreen"], """        composable<AdoptRoute> {
            AdoptScreen(onOpenCat = { catId -> navController.navigate(CatDetailRoute(catId)) })
        }"""),
    "detail_v1": (["com.thefelineco.ui.catdetail.CatDetailScreen", "SNACK"], """        composable<CatDetailRoute> {
            CatDetailScreen(
                onBack = { navController.popBackStack() },
                // Booking is built in the next stage; for now just acknowledge the tap.
                onBook = { cat -> scope.launch { snackbar.showSnackbar("Booking a visit with ${cat.name} is coming soon") } },
                onOpenCat = { catId -> navController.navigate(CatDetailRoute(catId)) },
            )
        }"""),
    "detail": (["com.thefelineco.ui.catdetail.CatDetailScreen", "androidx.activity.compose.rememberLauncherForActivityResult",
                "androidx.compose.runtime.getValue", "androidx.compose.runtime.mutableStateOf",
                "androidx.compose.runtime.saveable.rememberSaveable", "androidx.compose.runtime.setValue",
                "com.thefelineco.domain.model.Booking", "com.thefelineco.ui.booking.BookMeetAndGreet",
                "com.thefelineco.ui.common.formatLong", "com.thefelineco.ui.components.SuccessDialog"], """        composable<CatDetailRoute> {
            // Activity Result API: launch BookingActivity with the Cat, receive the Booking back.
            var confirmed by rememberSaveable { mutableStateOf<Booking?>(null) }
            val bookLauncher = rememberLauncherForActivityResult(BookMeetAndGreet()) { booking ->
                confirmed = booking
            }
            CatDetailScreen(
                onBack = { navController.popBackStack() },
                onBook = { cat -> bookLauncher.launch(cat) },
                onOpenCat = { catId -> navController.navigate(CatDetailRoute(catId)) },
            )
            confirmed?.let { booking ->
                SuccessDialog(
                    title = "You're booked in!",
                    message = "Your meet & greet with ${booking.catName} is on ${booking.date.formatLong()} at " +
                        "${booking.timeSlot}. " + if (booking.feeCredits > 0) "${booking.feeCredits} credits are on hold." else "",
                    confirmLabel = "View my bookings",
                    onConfirm = {
                        confirmed = null
                        navController.navigateToTopLevel(BookingsRoute)
                    },
                    dismissLabel = "Keep browsing",
                    onDismiss = { confirmed = null },
                )
            }
        }"""),
    "shop": (["com.thefelineco.ui.shop.ShopScreen"], """        composable<ShopRoute> { ShopScreen() }"""),
    "basket_v1": (["com.thefelineco.ui.basket.BasketScreen", "SNACK"], """        composable<BasketRoute> {
            BasketScreen(
                // Checkout is built in a later step; for now just acknowledge the tap.
                onCheckout = { scope.launch { snackbar.showSnackbar("Checkout is coming soon") } },
                onShop = { navController.navigateToTopLevel(ShopRoute) },
            )
        }"""),
    "basket": (["com.thefelineco.ui.basket.BasketScreen", "androidx.activity.compose.rememberLauncherForActivityResult",
                "androidx.compose.runtime.getValue", "androidx.compose.runtime.mutableStateOf",
                "androidx.compose.runtime.saveable.rememberSaveable", "androidx.compose.runtime.setValue",
                "com.thefelineco.domain.model.DeliveryMethod", "com.thefelineco.domain.model.Order",
                "com.thefelineco.ui.checkout.CheckoutContract", "com.thefelineco.ui.components.SuccessDialog"], """        composable<BasketRoute> {
            // Activity Result API: launch CheckoutActivity with the basket, receive the Order back.
            var placed by rememberSaveable { mutableStateOf<Order?>(null) }
            val checkoutLauncher = rememberLauncherForActivityResult(CheckoutContract()) { order -> placed = order }
            BasketScreen(
                onCheckout = { items -> checkoutLauncher.launch(items) },
                onShop = { navController.navigateToTopLevel(ShopRoute) },
            )
            placed?.let { order ->
                SuccessDialog(
                    title = "Order placed!",
                    message = "Order #${order.id} · ${order.itemCount} item${if (order.itemCount == 1) "" else "s"} · " +
                        "${order.total} credits. " +
                        if (order.deliveryMethod == DeliveryMethod.CLICK_AND_COLLECT) "It'll be ready to collect tomorrow."
                        else "It's on its way to ${order.deliveryName}.",
                    confirmLabel = "View my orders",
                    onConfirm = {
                        placed = null
                        navController.navigateToTopLevel(ProfileRoute)
                    },
                    dismissLabel = "Keep shopping",
                    onDismiss = {
                        placed = null
                        navController.navigateToTopLevel(ShopRoute)
                    },
                )
            }
        }"""),
    "bookings": (["com.thefelineco.ui.bookings.BookingsScreen"], """        composable<BookingsRoute> {
            BookingsScreen(onFindCat = { navController.navigateToTopLevel(AdoptRoute()) })
        }"""),
    "profile_v1": (["com.thefelineco.ui.common.ComingSoonScreen"], """        composable<ProfileRoute> {
            ComingSoonScreen(
                title = "Hi, ${user.firstName}",
                message = "${user.email} · ${user.credits} credits",
                actionLabel = "Log out",
                onAction = onLogout,
            )
        }"""),
    "profile": (["com.thefelineco.ui.profile.ProfileScreen"], """        composable<ProfileRoute> { ProfileScreen(onLogout = onLogout) }"""),
    "a_dash": (["com.thefelineco.ui.admin.AdminDashboardScreen"], """        composable<AdminDashboardRoute> {
            AdminDashboardScreen(
                onAddCat = { navController.navigate(AdminCatEditRoute()) },
                onOpenAppointments = { navController.navigateToTopLevel(AdminBookingsRoute) },
                onOpenProducts = { navController.navigateToTopLevel(AdminProductsRoute) },
            )
        }"""),
    "a_cats": (["com.thefelineco.ui.admin.AdminCatsScreen"], """        composable<AdminCatsRoute> {
            AdminCatsScreen(
                onEditCat = { catId -> navController.navigate(AdminCatEditRoute(catId)) },
                onAddCat = { navController.navigate(AdminCatEditRoute()) },
            )
        }"""),
    "a_catedit": (["com.thefelineco.ui.admin.CatFormScreen", "FORM"], """        composable<AdminCatEditRoute> {
            CatFormScreen(onBack = { navController.popBackStack() }, onDone = finishForm)
        }"""),
    "a_bookings": (["com.thefelineco.ui.admin.AdminBookingsScreen"], """        composable<AdminBookingsRoute> { AdminBookingsScreen() }"""),
    "a_products": (["com.thefelineco.ui.admin.AdminProductsScreen"], """        composable<AdminProductsRoute> {
            AdminProductsScreen(
                onEditProduct = { id -> navController.navigate(AdminProductEditRoute(id)) },
                onAddProduct = { navController.navigate(AdminProductEditRoute()) },
            )
        }"""),
    "a_productedit": (["com.thefelineco.ui.admin.ProductFormScreen", "FORM"], """        composable<AdminProductEditRoute> {
            ProductFormScreen(onBack = { navController.popBackStack() }, onDone = finishForm)
        }"""),
}


def gen_navhost(built):
    """built: dict route -> block key (e.g. {'home': 'home', 'detail': 'detail_v1'})."""
    imports = {"androidx.compose.runtime.Composable", "androidx.navigation.NavHostController",
               "androidx.navigation.compose.NavHost", "androidx.navigation.compose.composable",
               "com.thefelineco.domain.model.User"}
    needs_snack = needs_form = False
    blocks = {}
    for r in ROUTES:
        key = built.get(r)
        if key is None:
            if r == "profile":
                key = "profile_v1"
            else:
                imports.add("com.thefelineco.ui.common.ComingSoonScreen")
                blocks[r] = f"        composable<{ROUTE_CLASS[r]}> {{ ComingSoonScreen(\"{PLACEHOLDER_TITLE[r]}\") }}"
                continue
        imps, block = NAV_BLOCKS[key]
        for i in imps:
            if i == "SNACK":
                needs_snack = True
            elif i == "FORM":
                needs_form = needs_snack = True
            else:
                imports.add(i)
        blocks[r] = block
    pre = ""
    if needs_snack:
        imports |= {"androidx.compose.runtime.rememberCoroutineScope", "com.thefelineco.ui.common.LocalSnackbarHostState",
                    "kotlinx.coroutines.launch"}
        pre = """    // Lives as long as the NavHost, so a message survives the screen that triggered it closing.
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
"""
        if needs_form:
            pre += """    val finishForm: (String) -> Unit = { message ->
        navController.popBackStack()
        scope.launch { snackbar.showSnackbar(message) }
    }
"""
        pre += "\n"
    customer = "\n".join(blocks[r] for r in ROUTES[:7])
    admin = "\n".join(blocks[r] for r in ROUTES[7:])
    imp = "\n".join(f"import {i}" for i in sorted(imports))
    return f"""package com.thefelineco.ui.navigation

{imp}

/** Every in-app screen for a signed-in user. Secondary activities are launched from these screens. */
@Composable
fun FelineNavHost(
    navController: NavHostController,
    user: User,
    onLogout: () -> Unit,
) {{
{pre}    NavHost(
        navController = navController,
        startDestination = if (user.isAdmin) AdminDashboardRoute else HomeRoute,
    ) {{
        // Customer
{customer}

        // Admin
{admin}
    }}
}}
"""


COMING_SOON = open(f"{SCR}/ComingSoonScreen.kt").read().replace(
    "/** Temporary screen for destinations that are built in a later phase (see CONTEXT.md §9). */",
    "/** Temporary screen for destinations that haven't been built yet. Removed once every screen exists. */")

README_INTRO = """# The Feline Co.

A premium, cats-only adoption and pet-supply app for Android tablets, built with Kotlin and Jetpack Compose.

Customers browse cats, book a meet & greet, adopt with credits (cats aged two and over are free) and spend
their credits on food and toys. Admins manage listings, appointments and products.

> Work in progress: features are added stage by stage. See the commit history.
"""

README_FINAL = """# The Feline Co.

A premium, cats-only adoption and pet-supply app for Android tablets, built with Kotlin and Jetpack Compose.

Customers browse cats, book a meet & greet, adopt with credits (cats aged two and over are free) and spend
their credits on Royal Feline food and toys. Admins manage listings, appointments and products.

## Features

| Area | Highlights |
|---|---|
| Adopt | Search, filters (fee, age, sex, coat, compatibility, favourites), five sort orders, adaptive grid |
| Cat profile | Photo, story, personality, health and compatibility, fee vs. balance, similar cats |
| Booking | `BookingActivity`: date strip, live slot availability, validated form, fee held as credits |
| My bookings | Upcoming and past visits, cancel with automatic refund |
| Shop and basket | Categories, sorting, product sheet with stock limits, persistent basket |
| Checkout | `CheckoutActivity`: delivery options, conditional address fields, validation, credit checks |
| Account | Credit balance, wallet history, orders, dark/light theme toggle |
| Admin | Dashboard, add/edit/delete cats, confirm/decline/complete adoptions, product stock |
| Favourites | Heart any cat, filter by favourites, favourites row on Home |

## Architecture

MVVM with unidirectional data flow (`UiState` + events), Room for local data, DataStore for the
session and settings, manual dependency injection (`AppContainer`), and type-safe Navigation Compose.
`MainActivity` hosts every screen; `BookingActivity` and `CheckoutActivity` receive Parcelable data
and return their results through the Activity Result API.

```
ui (Compose screens + ViewModels)  →  domain (models, rules, validators)  ←  data (Room, repositories)
```

## Run it

1. Open the project in Android Studio and let Gradle sync.
2. Run the `app` configuration on a tablet or phone (minimum Android 8.0, API 26).

### Demo accounts

| Role | Email | Password |
|---|---|---|
| Customer (400 credits) | `demo@thefelineco.com` | `Demo123!` |
| Admin | `admin@thefelineco.com` | `Admin123!` |

The login screen also has one-tap **Customer** and **Admin** demo buttons.

## Tests

- **Unit tests:** `./gradlew test`, covering credit rules, search/filter/sort, validators and ViewModels.
- **Instrumented tests:** `./gradlew connectedAndroidTest` with a device connected, covering Room credit and stock flows and the login UI.
- **Manual test script:** see [`TESTING.md`](TESTING.md).
"""

# --------------------------------------------------------------------------------------------
# Stage list
# --------------------------------------------------------------------------------------------
def F(p):  # final file from source
    return ("final", p)


def P(p):  # pre-favourites version
    return ("prefav", p)


def D(p):
    return ("delete", p)


def W(p, content):
    return ("write", p, content)


STAGES = []


def sub(sid, slug, msg, files=(), feats=(), manifest=None, main=None, vm=(), nav=None, gradle=False):
    STAGES.append(dict(id=sid, slug=slug, msg=msg, files=list(files), feats=list(feats), manifest=manifest,
                       main=main, vm=list(vm), nav=nav, gradle=gradle))


# Stage 1 — project setup
sub("01.1", "gradle-project", "Set up Gradle project with version catalog and wrapper",
    [F("settings.gradle.kts"), F("gradle/wrapper/gradle-wrapper.jar"), F("gradle/wrapper/gradle-wrapper.properties"),
     F("gradlew"), F("gradlew.bat"), W(".gitignore", GITIGNORE)], feats=["base"], gradle=True)
sub("01.2", "app-module", "Add app module with a Compose MainActivity",
    [F("app/proguard-rules.pro"), F(R + "values/strings.xml"), F(R + "values/colors.xml"), F(R + "values/themes.xml"),
     F(R + "values-night/themes.xml")], manifest=1, main=MAIN_HELLO, gradle=True)
sub("01.3", "launcher-icon", "Add crown-cat adaptive launcher icon",
    [F(R + "drawable/ic_launcher_foreground.xml"), F(R + "mipmap-anydpi-v26/ic_launcher.xml"),
     F(R + "mipmap-anydpi-v26/ic_launcher_round.xml")])
# Stage 2 — brand
sub("02.1", "colours-shapes", "Define brand colour palette and shapes", [F(K + "ui/theme/Color.kt"), F(K + "ui/theme/Shape.kt")])
sub("02.2", "typography-theme", "Add typography with optional brand fonts and FelineTheme",
    [F(K + "ui/theme/Type.kt"), F(K + "ui/theme/Theme.kt")], main=MAIN_THEMED)
sub("02.3", "logo-paws", "Draw the logo and paw pattern on a Canvas",
    [F(K + "ui/components/FelineLogo.kt"), F(K + "ui/components/PawPattern.kt")], feats=["icons"], gradle=True)
sub("02.4", "splash", "Show branded splash screen on launch", [F(K + "ui/common/SplashScreen.kt")], main=MAIN_SPLASH)
# Stage 3 — domain
sub("03.1", "cat-model", "Add cat model, enums and credit rules",
    [F(K + "domain/model/Enums.kt"), F(K + "domain/model/Cat.kt"), F(K + "domain/CreditRules.kt")], feats=["parcelize"], gradle=True)
sub("03.2", "other-models", "Add booking, shop and user models",
    [F(K + "domain/model/Booking.kt"), F(K + "domain/model/Shop.kt"), F(K + "domain/model/User.kt")])
sub("03.3", "credit-tests", "Unit-test credit rules and age labels",
    [F(T + "domain/CreditRulesTest.kt"), F(T + "domain/CatAgeLabelTest.kt")], feats=["unittest"], gradle=True)
sub("03.4", "queries", "Add cat and product search, filter and sort queries",
    [P(K + "domain/CatQuery.kt"), F(K + "domain/ProductQuery.kt"), F(T + "domain/TestCats.kt"), P(T + "domain/CatQueryTest.kt")])
sub("03.5", "validators", "Add form validators with unit tests",
    [F(K + "domain/validation/Validators.kt"), F(T + "domain/ValidatorsTest.kt")])
# Stage 4 — components
sub("04.1", "badges-states", "Add badges, chips and empty/loading states",
    [F(K + "ui/components/Badges.kt"), F(K + "ui/components/States.kt")])
sub("04.2", "form-fields", "Add validated text and password fields", [F(K + "ui/components/FormFields.kt")])
sub("04.3", "asset-image", "Load local photos by name with a branded placeholder",
    [F(K + "ui/components/AssetImage.kt"), F(R + "drawable-nodpi/.gitkeep"), F(R + "raw/keep.xml")], feats=["coil"], gradle=True)
sub("04.4", "cards", "Add cat and product cards with previews",
    [P(K + "ui/components/CatCard.kt"), F(K + "ui/components/ProductCard.kt"), F(K + "ui/components/ComponentPreviews.kt")])
# Stage 5 — database
sub("05.1", "entities", "Add Room entities, mappers and type converters",
    [P(K + "data/local/entity/Entities.kt"), F(K + "data/local/entity/Mappers.kt"), F(K + "data/local/Converters.kt")],
    feats=["room"], gradle=True)
sub("05.2", "daos", "Add DAOs for users, cats, bookings and the shop",
    [F(K + "data/local/dao/UserDao.kt"), P(K + "data/local/dao/CatDao.kt"), F(K + "data/local/dao/BookingDao.kt"),
     F(K + "data/local/dao/ShopDao.kt")])
sub("05.3", "database", "Create the Room database, password hashing and wallet helper",
    [P(K + "data/local/FelineDatabase.kt"), F(K + "data/local/PasswordHasher.kt"), F(K + "data/local/Wallet.kt")])
sub("05.4", "seed", "Seed demo accounts, 24 cats and 18 products",
    [F(K + "data/local/seed/SeedData.kt"), F(K + "data/local/seed/DatabaseSeeder.kt")])
# Stage 6 — repositories
sub("06.1", "cat-user-repos", "Add cat and user repositories with a DataStore session",
    [F(K + "data/repository/FelineException.kt"), P(K + "data/repository/CatRepository.kt"),
     F(K + "data/repository/SessionStore.kt"), F(K + "data/repository/UserRepository.kt")], feats=["datastore"], gradle=True)
sub("06.2", "booking-repo", "Add booking repository with credit hold, refund and reward",
    [F(K + "data/repository/BookingRepository.kt")])
sub("06.3", "shop-repo", "Add shop repository with basket and checkout", [F(K + "data/repository/ShopRepository.kt")])
sub("06.4", "container", "Wire the app container and seed the database on first launch",
    [F(K + "data/repository/SettingsStore.kt"), F(K + "di/AppContainer.kt"), F(K + "FelineApplication.kt")], manifest=2)
sub("06.5", "flow-tests", "Add instrumented tests for adoption credits and checkout",
    [P(A + "data/AdoptionAndShopFlowTest.kt")], feats=["androidtest"], gradle=True)
# Stage 7 — auth
sub("07.1", "content-helpers", "Add shared activity theme helper and layout utilities",
    [F(K + "ui/common/FelineContent.kt"), F(K + "ui/common/Layout.kt")], feats=["lifecyclecompose"], gradle=True)
sub("07.2", "auth-viewmodels", "Add session, login and register ViewModels",
    [F(K + "ui/MainViewModel.kt"), F(K + "ui/auth/AuthViewModels.kt")], vm=["main", "login", "register"])
sub("07.3", "auth-screens", "Build login and register screens",
    [F(K + "ui/auth/AuthLayout.kt"), F(K + "ui/auth/FormErrorBanner.kt"), F(K + "ui/auth/LoginScreen.kt"),
     F(K + "ui/auth/RegisterScreen.kt"), F(K + "ui/auth/AuthNavHost.kt"), F(K + "ui/navigation/Routes.kt")],
    feats=["navigation"], gradle=True)
sub("07.4", "session-switch", "Switch between sign-in and the app based on the saved session", main=MAIN_AUTH)
sub("07.5", "login-ui-test", "Add Compose UI test for the login form", [F(A + "ui/LoginScreenTest.kt")], feats=["uitest"], gradle=True)
# Stage 8 — shell
sub("08.1", "destinations", "Add top-level destinations and a placeholder screen",
    [F(K + "ui/navigation/TopLevelDestination.kt"), W(K + "ui/common/ComingSoonScreen.kt", COMING_SOON)])
sub("08.2", "app-shell", "Add adaptive navigation shell (rail on tablets, bar on phones)",
    [F(K + "ui/navigation/FelineAppShell.kt")], feats=["navsuite"], gradle=True, main="FINAL", nav={})
# Stage 9 — home
sub("09.1", "home-viewmodel", "Add Home ViewModel combining cats, products and the user",
    [P(K + "ui/home/HomeViewModel.kt")], vm=["home"])
sub("09.2", "home-screen", "Build Home screen with hero, stats and carousels", [P(K + "ui/home/HomeScreen.kt")], nav={"home": "home"})
# Stage 10 — adopt
sub("10.1", "search-sort", "Add search field, sort menu and formatting helpers",
    [F(K + "ui/components/SearchAndSort.kt"), F(K + "ui/common/Formatters.kt")])
sub("10.2", "adopt-viewmodel", "Add Adopt ViewModel with unit tests and fake repositories",
    [P(K + "ui/adopt/AdoptViewModel.kt"), P(T + "testing/Fakes.kt"), F(T + "testing/MainDispatcherRule.kt"),
     P(T + "ui/AdoptViewModelTest.kt")], vm=["adopt_v1"], feats=["coroutinestest"], gradle=True)
sub("10.3", "filter-panel", "Add the filter panel and brand filter chips", [P(K + "ui/adopt/CatFilterPanel.kt")])
sub("10.4", "adopt-screen", "Build Adopt screen with responsive grid and filter sheet",
    [P(K + "ui/adopt/AdoptScreen.kt")], nav={"adopt": "adopt"})
# Stage 11 — cat profile
sub("11.1", "detail-viewmodel", "Add cat profile ViewModel", [P(K + "ui/catdetail/CatDetailViewModel.kt")], vm=["detail"])
sub("11.2", "detail-screen", "Build the cat profile screen", [P(K + "ui/catdetail/CatDetailScreen.kt")], nav={"detail": "detail_v1"})
# Stage 12 — booking activity
sub("12.1", "dialogs-datestrip", "Add confirm/success dialogs and a date strip",
    [F(K + "ui/components/Dialogs.kt"), F(K + "ui/components/DateStrip.kt")])
sub("12.2", "booking-viewmodel", "Add booking ViewModel with validation and unit tests",
    [F(K + "ui/booking/BookingViewModel.kt"), F(T + "ui/BookingViewModelTest.kt")])
sub("12.3", "booking-activity", "Add BookingActivity with the meet & greet form",
    [F(K + "ui/booking/BookMeetAndGreet.kt"), F(K + "ui/booking/BookingActivity.kt"), F(K + "ui/booking/BookingScreen.kt")], manifest=3)
sub("12.4", "launch-booking", "Launch booking from the cat profile and confirm the result", nav={"detail": "detail"})
# Stage 13 — my bookings
sub("13.1", "booking-card", "Add booking card component", [F(K + "ui/components/BookingCard.kt")])
sub("13.2", "my-bookings", "Build My Bookings with cancel and refund",
    [F(K + "ui/bookings/BookingsViewModel.kt"), F(K + "ui/bookings/BookingsScreen.kt")], vm=["bookings"], nav={"bookings": "bookings"})
# Stage 14 — shop
sub("14.1", "shop-viewmodel", "Add shop ViewModel and quantity stepper",
    [F(K + "ui/shop/ShopViewModel.kt"), F(K + "ui/components/QuantityStepper.kt")], vm=["shop"])
sub("14.2", "shop-screen", "Build the shop with categories and a product sheet", [F(K + "ui/shop/ShopScreen.kt")], nav={"shop": "shop"})
# Stage 15 — basket & checkout
sub("15.1", "basket", "Build the basket with live totals",
    [F(K + "ui/basket/BasketViewModel.kt"), F(K + "ui/basket/BasketScreen.kt")], vm=["basket"], nav={"basket": "basket_v1"})
sub("15.2", "checkout-viewmodel", "Add checkout ViewModel with unit tests",
    [F(K + "ui/checkout/CheckoutViewModel.kt"), F(T + "ui/CheckoutViewModelTest.kt")])
sub("15.3", "checkout-activity", "Add CheckoutActivity with the delivery form",
    [F(K + "ui/checkout/CheckoutContract.kt"), F(K + "ui/checkout/CheckoutActivity.kt"), F(K + "ui/checkout/CheckoutScreen.kt")], manifest=4)
sub("15.4", "launch-checkout", "Launch checkout from the basket and confirm the order", nav={"basket": "basket"})
# Stage 16 — account
sub("16.1", "profile-viewmodel", "Add account ViewModel with wallet, orders and theme setting",
    [F(K + "ui/profile/ProfileViewModel.kt")], vm=["profile"])
sub("16.2", "profile-screen", "Build the account screen with wallet history and theme toggle",
    [F(K + "ui/profile/ProfileScreen.kt")], nav={"profile": "profile"})
# Stage 17 — admin
sub("17.1", "admin-components", "Add shared admin components", [F(K + "ui/admin/AdminComponents.kt")])
sub("17.2", "admin-dashboard", "Build the admin dashboard", [F(K + "ui/admin/AdminDashboard.kt")], vm=["a_dash"], nav={"a_dash": "a_dash"})
sub("17.3", "admin-cats", "Add listing management with search and status filter",
    [F(K + "ui/admin/AdminCatsScreen.kt")], vm=["a_cats"], nav={"a_cats": "a_cats"})
sub("17.4", "cat-form", "Add the cat add/edit form with validation",
    [F(K + "ui/admin/CatFormViewModel.kt"), F(K + "ui/admin/CatFormScreen.kt")], vm=["a_catform"], nav={"a_catedit": "a_catedit"})
sub("17.5", "admin-bookings", "Add appointments management for admins",
    [F(K + "ui/admin/AdminBookingsScreen.kt")], vm=["a_bookings"], nav={"a_bookings": "a_bookings"})
sub("17.6", "admin-products", "Add product management and remove the placeholder screen",
    [F(K + "ui/admin/AdminProductsScreen.kt"), F(K + "ui/admin/ProductFormScreen.kt"), D(K + "ui/common/ComingSoonScreen.kt")],
    vm=["a_products", "a_productform"], nav={"a_products": "a_products", "a_productedit": "a_productedit"})
# Stage 18 — favourites
sub("18.1", "favourites-data", "Store favourites in Room (database version 2)",
    [F(K + "data/local/entity/Entities.kt"), F(K + "data/local/dao/CatDao.kt"), F(K + "data/local/FelineDatabase.kt"),
     F(K + "data/repository/CatRepository.kt"), F(T + "testing/Fakes.kt"), F(A + "data/AdoptionAndShopFlowTest.kt")])
sub("18.2", "favourites-ui-parts", "Add favourites filter and animated heart button",
    [F(K + "domain/CatQuery.kt"), F(T + "domain/CatQueryTest.kt"), F(K + "ui/common/Favourites.kt"), F(K + "ui/components/CatCard.kt")])
sub("18.3", "favourites-screens", "Show favourites on Adopt, cat profiles and Home",
    [F(K + "ui/adopt/AdoptViewModel.kt"), F(K + "ui/adopt/AdoptScreen.kt"), F(K + "ui/adopt/CatFilterPanel.kt"),
     F(K + "ui/catdetail/CatDetailViewModel.kt"), F(K + "ui/catdetail/CatDetailScreen.kt"), F(K + "ui/home/HomeViewModel.kt"),
     F(K + "ui/home/HomeScreen.kt"), F(T + "ui/AdoptViewModelTest.kt")], vm=["adopt"])
# Stage 19 — docs
sub("19.1", "readme", "Write README with features, architecture and demo accounts", [W("README.md", README_FINAL)])
sub("19.2", "testing-guide", "Add testing guide with the manual test script", [F("TESTING.md")])


# --------------------------------------------------------------------------------------------
# Runner
# --------------------------------------------------------------------------------------------
def sh(cmd, cwd=OUT, check=True):
    r = subprocess.run(cmd, cwd=cwd, shell=True, capture_output=True, text=True)
    if check and r.returncode != 0:
        print(r.stdout, r.stderr)
        raise SystemExit(f"command failed: {cmd}")
    return r.stdout


def write(path, content, mode=None):
    full = os.path.join(OUT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        f.write(content)


def copy(src_root, path):
    full = os.path.join(OUT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    shutil.copy2(os.path.join(src_root, path), full)


def main():
    if os.path.exists(OUT):
        shutil.rmtree(OUT)
    os.makedirs(OUT)
    sh("git init -q -b main && git config user.email stager@example.com && git config user.name stager")
    write("README.md", README_INTRO)
    sh("git add -A && git commit -q -m 'Base: README intro (step 00.1, done by hand)'")
    feats, vms, nav, manifest_level = set(), [], {}, 0
    for st in STAGES:
        for op in st["files"]:
            kind = op[0]
            if kind == "final":
                copy(SRC, op[1])
            elif kind == "prefav":
                src = PREFAV if os.path.exists(os.path.join(PREFAV, op[1])) else SRC
                copy(src, op[1])
            elif kind == "delete":
                os.remove(os.path.join(OUT, op[1]))
            elif kind == "write":
                write(op[1], op[2])
        feats |= set(st["feats"])
        if st["gradle"]:
            write("gradle/libs.versions.toml", gen_catalog(feats))
            write("build.gradle.kts", gen_root_build(feats))
            write("gradle.properties", gen_gradle_properties(feats))
            if "settings.gradle.kts" not in [o[1] for o in st["files"]]:
                pass
            if os.path.exists(os.path.join(OUT, "app")) or st["id"] != "01.1":
                write("app/build.gradle.kts", gen_app_build(feats))
        if st["manifest"]:
            manifest_level = st["manifest"]
            write("app/src/main/AndroidManifest.xml", gen_manifest(manifest_level))
        if st["main"]:
            if st["main"] == "FINAL":
                copy(SRC, K + "MainActivity.kt")
            else:
                write(K + "MainActivity.kt", st["main"])
        if st["vm"]:
            for v in st["vm"]:
                if v == "adopt":
                    vms = [x for x in vms if x != "adopt_v1"]
                vms.append(v)
            write(K + "di/AppViewModelProvider.kt", gen_vmp(vms))
        if st["nav"] is not None:
            nav.update(st["nav"])
            write(K + "ui/navigation/FelineNavHost.kt", gen_navhost(nav))
        sh("git add -A")
        sh(f"git commit -q -m {json.dumps(st['id'] + ' ' + st['msg'])}")
        sh(f"git tag s{st['id']}")
    print(f"{len(STAGES)} substages committed in {OUT}")


if __name__ == "__main__":
    main()
