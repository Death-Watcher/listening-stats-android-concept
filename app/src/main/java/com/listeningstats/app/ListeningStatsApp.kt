package com.listeningstats.app

import android.app.Application

class ListeningStatsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ListeningStatsApp
            private set
    }
}
