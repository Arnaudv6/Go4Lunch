package com.cleanup.go4lunch.data.users

data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String?,
    val visitedPlaces: LongArray,
    val likedPlaces: LongArray,
    val goingAtNoon: Long?,
    // timestamp goingAtNoon?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (avatarUrl != other.avatarUrl) return false
        if (!visitedPlaces.contentEquals(other.visitedPlaces)) return false
        if (!likedPlaces.contentEquals(other.likedPlaces)) return false
        if (goingAtNoon != other.goingAtNoon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + visitedPlaces.contentHashCode()
        result = 31 * result + likedPlaces.contentHashCode()
        result = 31 * result + (goingAtNoon?.hashCode() ?: 0)
        return result
    }
}

