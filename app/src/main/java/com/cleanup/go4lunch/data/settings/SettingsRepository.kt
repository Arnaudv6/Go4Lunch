package com.cleanup.go4lunch.data.settings

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    @ApplicationContext appContext: Context,
    private val settingsDao: SettingsDao,
    private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        private val FRANCE_BOX = BoundingBox(51.404, 8.341, 42.190, -4.932)
    }

    suspend fun getInitialBox(): BoundingBox = run {
        val box = settingsDao.getBox()
        if (box == null) FRANCE_BOX
        else BoundingBox(
            box.north,
            box.east,
            box.south,
            box.west
        )
    }

    fun setMapBox(boxEntity: BoxEntity) {
        CoroutineScope(ioDispatcher).launch {
            settingsDao.setBox(boxEntity)
        }
    }


    val isNightTheme = appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    // Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_NIGHT_NO
    // Nino, comment tu l'appliques, le dark theme? https://stackoverflow.com/a/56036734

}


