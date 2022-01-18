package com.freemyip.arnaudv6.go4lunch.ui.detail

sealed class DetailsViewAction {
    data class Call(val number: String) : DetailsViewAction()
    data class Surf(val address: String) : DetailsViewAction()
}

