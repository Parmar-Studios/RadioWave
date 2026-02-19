package com.parmarstudios.radiowave

import android.app.Application
import android.content.Context

class RadioWaveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: RadioWaveApp
        fun appContext(): Context = instance.applicationContext
    }
}