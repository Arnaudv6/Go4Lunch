package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UseCase
@Inject constructor(
    private val usersRepository: UsersRepository
) {
    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListFlow: Flow<List<User>> = matesListMutableStateFlow.asStateFlow()

    suspend operator fun invoke() {
        usersRepository.getUsersList()?.let { matesListMutableStateFlow.tryEmit(it) }
    }

    // TODO use a flow here, don't "query" directly on the "livedata / hotflow"
    fun usersGoingThere(osmId: Long): List<User> = matesListMutableStateFlow
        .value.filter { it.goingAtNoon == osmId }

}


