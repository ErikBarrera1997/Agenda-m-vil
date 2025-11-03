package com.dev.agenda_movil

import android.app.Application
import com.dev.di.AppContainer

class AgendaApplication : Application()  {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }


}