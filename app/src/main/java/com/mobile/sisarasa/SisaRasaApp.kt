package com.mobile.sisarasa

import android.app.Application
import com.mobile.sisarasa.di.AppContainer

class SisaRasaApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}