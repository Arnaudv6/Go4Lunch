package com.cleanup.go4lunch.ui.mates

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class MatesViewModel @Inject constructor(
    private val application: Application,  // appContext notation gives a harmless "leak" linter warning.
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    suspend fun swipeRefresh() {
        usersRepository.updateMatesList()
    }

    // don't filter sessionUser out (nor in detailsVM) as list would refresh when not networkIsAvailable
    val mMatesListLiveData: LiveData<List<MatesViewStateItem>> =
        usersRepository.matesListFlow.mapNotNull {
            // mapNotNull is about keeping mates when connection lost
            it.map { user ->
                MatesViewStateItem(
                    mateId = user.id,
                    placeId = user.goingAtNoon,
                    imageUrl = user.avatarUrl.orEmpty(),
                    text = getText(user)
                )
            }
        }.asLiveData()

    private suspend fun getText(user: User): String {
        if (user.goingAtNoon == null) return application.getString(R.string.not_decided_yet)
            .format(user.firstName)
        val restaurant = poiRepository.getPoiById(user.goingAtNoon)
            ?: return application.getString(R.string.chose_restaurant_pid)
                .format(user.firstName, user.goingAtNoon)
        if (restaurant.cuisine.isEmpty()) return application.getString(R.string.chose_restaurant_name)
            .format(user.firstName, restaurant.name)
        return application.getString(R.string.chose_restaurant_cuisine)
            .format(user.firstName, restaurant.cuisine, restaurant.name)
    }
}


