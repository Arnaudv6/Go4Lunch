package com.cleanup.go4lunch.data.users

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class UsersRepository @Inject constructor(
    private val userRetrofit: UserRetrofit,
) {

    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListStateFlow: StateFlow<List<User>> = matesListMutableStateFlow

    suspend fun requestMatesRefresh() {
        matesListMutableStateFlow.tryEmit(userRetrofit.getUsers().map {
            User(
                id = it.id,
                firstName = it.firstName,
                lastName = it.lastName,
                avatarUrl = it.avatarUrl,
                goingAtNoon = it.goingAtNoon
            )
        })
    }

    fun usersGoing(osmId: Long): List<User> {
        return matesListMutableStateFlow.value.filter {
            it.goingAtNoon == osmId
        }
    }

    suspend fun insertUser(user: User) {
        userRetrofit.insertUser(
            UserBody(
                id = user.id,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
                goingAtNoon = user.goingAtNoon
            )
        )
    }

    fun likes(restaurantId: Long): Int {
        return Random.nextInt(3)
        // todo make this code relevant (notes de google par exemple)
    }

    fun goingAtNoon(): Long = 135423

    fun like(osmId: Long): Boolean = osmId % 2 == 0L

}
