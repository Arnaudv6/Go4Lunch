package com.cleanup.go4lunch.data.users

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor() {

    fun usersGoing(placeId: Long): List<String> {
        return if (placeId % 2 == 0L) listOf("Kevin", "Sasha") else emptyList()
    }

}
