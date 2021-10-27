package com.cleanup.go4lunch.ui.main

import android.app.Activity
import android.content.Intent

interface ActivityLauncher {
    fun launch(intent: Intent)
    fun getCaller(): Activity
}
