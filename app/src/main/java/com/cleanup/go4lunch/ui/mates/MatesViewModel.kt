package com.cleanup.go4lunch.ui.mates

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class MatesViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val poiRepository: PoiRepository
) : ViewModel() {

    val matesListLiveData: LiveData<List<Mate>> =
        usersRepository.matesListStateFlow.mapNotNull {
            it.map { user ->
                Mate(
                    id = user.id,
                    imageUrl = user.avatarUrl ?: "",
                    text = getText(user)
                )
            }
        }.asLiveData()

    fun refreshMatesList() {
        viewModelScope.launch(Dispatchers.IO) {
            usersRepository.requestMatesRefresh()
        }
    }

    private suspend fun getText(user: User): String {
        if (user.goingAtNoon == null) return "${user.firstName} has not decided yet"
        val restaurant = poiRepository.getPoiById(user.goingAtNoon)
            ?: return "${user.firstName}: chose restaurant id ${user.goingAtNoon}"
        if (restaurant.cuisine.isEmpty()) return "${user.firstName} is eating at ${restaurant.name}"
        return "${user.firstName} is eating ${restaurant.cuisine} (${restaurant.name})"
    }
}


