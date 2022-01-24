package com.freemyip.arnaudv6.go4lunch.data.users

data class User(
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String?,
    val goingAtNoon: Long? = null,
    val liked: LongArray? = null,
    // timestamp goingAtNoon?
) {
    fun isSameId(user: User): Boolean = this.email == user.email

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (email != other.email) return false
        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (avatarUrl != other.avatarUrl) return false
        if (goingAtNoon != other.goingAtNoon) return false
        if (liked != null) {
            if (other.liked == null) return false
            if (!liked.contentEquals(other.liked)) return false
        } else if (other.liked != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = email.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + (avatarUrl?.hashCode() ?: 0)
        result = 31 * result + (goingAtNoon?.hashCode() ?: 0)
        result = 31 * result + (liked?.contentHashCode() ?: 0)
        return result
    }
}

