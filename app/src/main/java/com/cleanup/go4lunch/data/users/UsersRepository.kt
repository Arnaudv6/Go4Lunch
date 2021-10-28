package com.cleanup.go4lunch.data.users

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class UsersRepository @Inject constructor(
    private val userRetrofit: UserRetrofit,
) {

    suspend fun usersGoing(osmId: Long): List<User> {
        // todo implement
        return getUsers()
    }

    suspend fun getUsers(): List<User> {
        return userRetrofit.getUsers().map {
            User(it.id, it.firstName, it.lastName, it.avatarUrl, it.goingAtNoon)
        }
    }

    suspend fun insertUser(user: User) {
        userRetrofit.insertUser(
            user.id,
            user.firstName,
            user.lastName,
            user.avatarUrl,
            user.goingAtNoon
        )
    }

    fun likes(restaurantId: Long): Int {
        return Random.nextInt(3)
        // todo make this code relevant (notes de google par exemple)
    }

    fun goingAtNoon(): Long = 135423

    fun like(osmId: Long): Boolean = osmId % 2 == 0L

}
