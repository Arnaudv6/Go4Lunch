package com.cleanup.go4lunch.ui.mates

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class MatesViewModel @Inject constructor(
    private val application: Application,  // appContext notation gives a harmless "leak" linter warning.
    poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    suspend fun swipeRefresh() = usersRepository.updateMatesList()

    // don't filter sessionUser out (nor in detailsVM) as list would refresh when not networkIsAvailable
    val mMatesListLiveData: LiveData<List<MatesViewStateItem>> = combine(
        usersRepository.matesListFlow.filterNotNull(),
        poiRepository.cachedPOIsListFlow.filterNotNull()
    ) { mates, places ->
        mates.map { user ->
            MatesViewStateItem(
                mateId = user.id,
                placeId = user.goingAtNoon,
                imageUrl = user.avatarUrl.orEmpty(),
                text = getText(user, places)
            )
        }
    }.filterNotNull().asLiveData()

    private fun getText(user: User, places: List<PoiEntity>): String {
        if (user.goingAtNoon == null) return application.getString(R.string.not_decided_yet)
            .format(user.firstName)
        val restaurant = places.firstOrNull { it.id == user.goingAtNoon }
            ?: return application.getString(R.string.chose_restaurant_pid)
                .format(user.firstName, user.goingAtNoon)
        if (restaurant.cuisine.isEmpty()) return application.getString(R.string.chose_restaurant_name)
            .format(user.firstName, restaurant.name)
        return application.getString(R.string.chose_restaurant_cuisine)
            .format(user.firstName, restaurant.cuisine, restaurant.name)
    }
}


