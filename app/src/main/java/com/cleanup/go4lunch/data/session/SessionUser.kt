package com.cleanup.go4lunch.data.session

import com.cleanup.go4lunch.data.users.User

data class SessionUser(
    val user: User,
    val liked: LongArray,
    val connectedThrough: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionUser

        if (user != other.user) return false
        if (!liked.contentEquals(other.liked)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + liked.contentHashCode()
        return result
    }
}

