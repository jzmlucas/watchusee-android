package br.com.watchusee.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class WatchuSeeApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
