package com.cleanup.go4lunch.data.users

data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String?,
    val goingAtNoon: Long?,
    // timestamp goingAtNoon?
)

