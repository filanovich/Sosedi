package by.imlab.sosedi

import android.app.Application
import by.imlab.core.KoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.*

private val koinModules = ServiceLoader.load(KoinModule::class.java).flatMap { it.modulesList }

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidLogger(Level.NONE)
            androidFileProperties()
            fragmentFactory()
            modules(koinModules)
        }
    }
}