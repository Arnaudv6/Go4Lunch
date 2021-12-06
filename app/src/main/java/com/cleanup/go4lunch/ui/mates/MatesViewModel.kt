package com.cleanup.go4lunch.ui.mates

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

@HiltViewModel
class MatesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    val poiRetrievalNumberSingleLiveEvent: SingleLiveEvent<Int> = SingleLiveEvent()

    suspend fun swipeRefresh() {
        usersRepository.updateMatesList()

        // todo this must happen here
        //  poiRetrievalNumberSingleLiveEvent.value
    }

    // don't filter sessionUser out (nor in detailsVM) as list would refresh when not networkIsAvailable
    // todo Arnaud : move this to mainViewModel? Usecase?
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
        if (user.goingAtNoon == null) return appContext.getString(R.string.not_decided_yet).format(user.firstName)
        val restaurant = poiRepository.getPoiById(user.goingAtNoon)
            ?: return appContext.getString(R.string.chose_restaurant_pid).format(user.firstName, user.goingAtNoon)
        if (restaurant.cuisine.isEmpty()) return appContext.getString(R.string.chose_restaurant_name).format(user.firstName, restaurant.name)
        return appContext.getString(R.string.chose_restaurant_cuisine).format(user.firstName, restaurant.cuisine, restaurant.name)
    }
}


