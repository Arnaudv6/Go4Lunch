package com.cleanup.go4lunch.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.exhaustive
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val poiRepository: PoiRepository,
    @ApplicationContext val appContext: Context
    // todo Nino : pourquoi me fait-il chier, là? parce que c'est un sous fragment?
) : ViewModel() {

    fun themeSet(theme: Any) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                appContext.getString(R.string.preferences_theme_key_dark) -> AppCompatDelegate.MODE_NIGHT_YES
                appContext.getString(R.string.preferences_theme_key_light) -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }.exhaustive
        )
    }

    fun clearCache(){
        viewModelScope.launch (Dispatchers.IO){
            poiRepository.clearCache()
        }
    }

}

