package com.cleanup.go4lunch

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application()

// this can be accessed with:
//  @ApplicationContext appContext: Context
// ou
//  application: Application
