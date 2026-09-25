package com.thefelineco

import android.app.Application
import com.thefelineco.di.AppContainer
import com.thefelineco.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Application entry point. It owns the dependency container and seeds the database on first launch. */
class FelineApplication : Application() {

    lateinit var container: AppContainer
        private set

    /** Lives as long as the app process. Used for work that must outlive any single screen. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        applicationScope.launch { container.seeder.seedIfEmpty() }
    }
}
