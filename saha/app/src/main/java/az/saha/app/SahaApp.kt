package az.saha.app

import android.app.Application
import az.saha.app.di.AppContainer

class SahaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
