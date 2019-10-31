package pl.mobilization.livedataarchitecture

import android.app.Application
import timber.log.Timber


class LiveDataApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}