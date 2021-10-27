package com.cleanup.go4lunch.data.users

data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String,
    // visitedPlaces: Array<Int>,
    // likedPlaces: Array<Int>,
    // separate Entity? 3 columns: keyId, userId, restauId
    val goingAtNoon: Long,
    // timestamp goingAtNoon?
)
