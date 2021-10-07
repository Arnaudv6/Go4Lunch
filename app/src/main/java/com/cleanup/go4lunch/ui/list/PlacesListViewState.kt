package com.cleanup.go4lunch.ui.list

import android.graphics.Bitmap

data class PlacesListViewState(
    val name: String,
    val address: String,
    val distance: String,
    val colleagues: Int,
    val image: Bitmap?,
    val hours: String,
    val likes: Int
)

