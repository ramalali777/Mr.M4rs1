package az.saha.app

import android.app.Application
import az.saha.app.di.AppContainer
import org.osmdroid.config.Configuration

class SahaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val osmPrefs = getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().load(this, osmPrefs)
        Configuration.getInstance().userAgentValue = packageName
        container = AppContainer(this)
    }
}
