package com.cleanup.go4lunch.ui.mates

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class MatesViewModel @Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    val poiRetrievalNumberSingleLiveEvent: SingleLiveEvent<Int> = SingleLiveEvent()

    suspend fun swipeRefresh() {
        usersRepository.updateMatesList()
        updateRatings()  // todo emerge a UseCase for this

        // todo this must happen here
        //  poiRetrievalNumberSingleLiveEvent.value
    }

    private suspend fun updateRatings() {
        val visited = usersRepository.getVisitedPlaceIds() ?: LongArray(0)
        val liked = usersRepository.getLikedPlaceIds() ?: LongArray(0)
        for (place in (visited + liked).toSet()) {
            // avoid dividing by zero.
            val ratio = liked.count { it == place } / (visited.count { it == place }.toFloat() + .1)
            poiRepository.updatePoiRating(
                place, when {
                    ratio < 0.2 -> 1
                    ratio < 0.3 -> 2
                    else -> 3
                }
            )
        }
    }

    // don't filter sessionUser out (nor in detailsVM) as list would refresh when not networkIsAvailable
    val mMatesListLiveData: LiveData<List<MatesViewStateItem>> =
        usersRepository.matesListFlow.mapNotNull {
            // todo though value is received here
            poiRepository.fetchPOIsInList(
                ids = it.mapNotNull { user -> user.goingAtNoon },
                refreshExisting = false
            )

            it.map { user ->
                MatesViewStateItem(
                    mateId = user.id,
                    placeId = user.goingAtNoon,
                    imageUrl = user.avatarUrl ?: "",
                    text = getText(user)
                )
            }
        }.asLiveData()

    private suspend fun getText(user: User): String {
        if (user.goingAtNoon == null) return "${user.firstName} has not decided yet"
        val restaurant = poiRepository.getPoiById(user.goingAtNoon)
            ?: return "${user.firstName}: chose restaurant id ${user.goingAtNoon}"
        if (restaurant.cuisine.isEmpty()) return "${user.firstName} is eating at ${restaurant.name}"
        return "${user.firstName} is eating ${restaurant.cuisine} (${restaurant.name})"
    }
}


