package com.cleanup.go4lunch.ui.main

sealed class MainViewAction {
    data class LaunchDetail(val osmId: Long) : MainViewAction()
    data class SnackBar(val message: String) : MainViewAction()
}

