package com.freemyip.go4lunch.ui.list

import androidx.annotation.ColorInt

data class PlacesListViewState(
    val id: Long,
    val name: String,
    val address: String,
    val distanceText: String,
    val mates: String,
    val image: String,
    val hours: String,
    @ColorInt
    val hoursColor: Int,
    val rating: Float?
)

