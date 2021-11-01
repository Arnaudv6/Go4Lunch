package com.cleanup.go4lunch.data.users

import com.cleanup.go4lunch.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

@Singleton
class UsersRepository @Inject constructor(
    private val userRetrofit: UserRetrofit,
    private val settingsRepository: SettingsRepository,
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

    // Todo Nino : comment je fais du stateIn dans le repo?
    val sessionUserFlow: StateFlow<User?> =
        matesListStateFlow.combine(settingsRepository.idStateFlow) { users, id ->
            users.find { it.id == id }
        }

    fun usersGoingAtPlaceId(osmId: Long): List<User> {
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
        // todo make this code relevant (google ratings?)
    }

    /** this is tricky, we update user online, and locally, to avoid full refresh */
    fun like(osmId: Long){
        sessionUserFlow.
        userRetrofit.insertUser()
    }

}
