package com.cleanup.go4lunch.ui.list

import android.graphics.Bitmap
import androidx.annotation.ColorInt

data class PlacesListViewState(
    val id: Long,
    val name: String,
    val address: String,
    val distance: Int?,
    val distanceText: String,
    val colleagues: String,
    val image: String,
    val hours: String,
    @ColorInt
    val hoursColor: Int,
    val likes: Float
)

