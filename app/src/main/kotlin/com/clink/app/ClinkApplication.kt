package com.clink.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for CLINK.
 * Annotated with [@HiltAndroidApp] to trigger Hilt's code generation.
 */
@HiltAndroidApp
class ClinkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
