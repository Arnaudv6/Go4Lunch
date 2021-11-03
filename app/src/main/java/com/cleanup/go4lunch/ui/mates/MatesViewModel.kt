package com.cleanup.go4lunch.ui.mates

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.data.UseCase
import com.cleanup.go4lunch.data.users.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MatesViewModel @Inject constructor(
    private val useCase: UseCase,
) : ViewModel() {

    suspend fun swipeRefresh() = useCase.updateUsers()

    val mMatesListLiveData: LiveData<List<MatesViewStateItem>> =
        useCase.matesListFlow.mapNotNull {
            it.map { user ->
                MatesViewStateItem(
                    id = user.id,
                    imageUrl = user.avatarUrl ?: "",
                    text = getText(user)
                )
            }
        }.asLiveData()

    private suspend fun getText(user: User): String {
        if (user.goingAtNoon == null) return "${user.firstName} has not decided yet"
        val restaurant = useCase.getPoiById(user.goingAtNoon)
            ?: return "${user.firstName}: chose restaurant id ${user.goingAtNoon}"
        if (restaurant.cuisine.isEmpty()) return "${user.firstName} is eating at ${restaurant.name}"
        return "${user.firstName} is eating ${restaurant.cuisine} (${restaurant.name})"
    }
}


