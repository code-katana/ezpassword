package com.codekatana.passwordgen

import android.app.Application
import com.codekatana.passwordgen.brains.WordBank

val wordBank by lazy { WordBank() }

class EzPassApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

    }

    companion object {
        lateinit var instance: EzPassApplication
            private set
    }
}