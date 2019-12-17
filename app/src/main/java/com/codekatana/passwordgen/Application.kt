package com.codekatana.passwordgen

import android.app.Application
import com.codekatana.passwordgen.brains.WordBank

class EzPassApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = this

    }

    companion object {
        lateinit var context: EzPassApplication
            private set
        val wordBank by lazy { WordBank() }
        val wordProcessor: WordProcessor by lazy { WordProcessor() }
    }
}