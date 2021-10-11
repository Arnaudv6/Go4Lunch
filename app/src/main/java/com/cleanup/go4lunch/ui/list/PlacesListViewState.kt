package com.cleanup.go4lunch.ui.list

import android.graphics.Bitmap

data class PlacesListViewState(
    val id: Long,
    val name: String,
    val address: String,
    val distance: Int?,
    val colleagues: Int,
    val image: Bitmap?,
    val hours: String,
    val likes: Int
)

