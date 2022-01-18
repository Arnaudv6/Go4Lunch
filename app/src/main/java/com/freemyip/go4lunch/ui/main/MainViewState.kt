package com.freemyip.go4lunch.ui.main

data class MainViewState(
    val avatarUrl: String?,
    val name: String,
    val connectedVia: String,
    val goingAtNoon: Long?
)
