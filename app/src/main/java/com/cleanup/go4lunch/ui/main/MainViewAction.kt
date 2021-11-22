package com.cleanup.go4lunch.ui.main

sealed class MainViewAction {
    data class LaunchDetail(val osmId: Long) : MainViewAction()
    data class StartNavNum(val number: Int) : MainViewAction()
}

