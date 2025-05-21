package com.example.elixir

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class Elixir : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}