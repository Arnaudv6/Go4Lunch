package com.cleanup.go4lunch.ui.mates

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.UseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class MatesViewModel @Inject constructor(
    private val useCase: UseCase,
    private val poiRepository: PoiRepository
) : ViewModel() {

    val poiRetrievalNumberSingleLiveEvent: SingleLiveEvent<Int> = SingleLiveEvent()

    suspend fun swipeRefresh() {
        useCase.updateUsers()
        // todo this must happen here
        //  poiRetrievalNumberSingleLiveEvent.value
    }

    val mMatesListLiveData: LiveData<List<MatesViewStateItem>> =
        useCase.matesListFlow.mapNotNull {
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
            // todo Nino: filter myself out? (and do so in detailsVM?)
        }.asLiveData()

    private suspend fun getText(user: User): String {
        if (user.goingAtNoon == null) return "${user.firstName} has not decided yet"
        val restaurant = poiRepository.getPoiById(user.goingAtNoon)
            ?: return "${user.firstName}: chose restaurant id ${user.goingAtNoon}"
        if (restaurant.cuisine.isEmpty()) return "${user.firstName} is eating at ${restaurant.name}"
        return "${user.firstName} is eating ${restaurant.cuisine} (${restaurant.name})"
    }
}


