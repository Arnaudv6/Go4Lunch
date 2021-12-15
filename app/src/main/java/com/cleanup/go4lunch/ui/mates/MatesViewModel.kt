package com.cleanup.go4lunch.ui.mates

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.SingleLiveEvent
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

    private val greyColor = ContextCompat.getColor(application, R.color.grey)
    val mateClickSingleLiveEvent: SingleLiveEvent<Long> = SingleLiveEvent()

    suspend fun swipeRefresh() = usersRepository.updateMatesList()

    fun mateClicked(placeId: Long?) = placeId?.let { mateClickSingleLiveEvent.postValue(it) }

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

    private fun getText(user: User, places: List<PoiEntity>): CharSequence {
        if (user.goingAtNoon == null) {
            val text = application.getString(R.string.not_decided_yet).format(user.firstName)
            return HtmlCompat.fromHtml(
                "<i><font color='$greyColor'>$text</font></i>",
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }
        val restaurant = places.firstOrNull { it.id == user.goingAtNoon }
            ?: return application.getString(R.string.chose_restaurant_pid)
                .format(user.firstName, user.goingAtNoon)
        if (restaurant.cuisine.isEmpty()) return application.getString(R.string.chose_restaurant_name)
            .format(user.firstName, restaurant.name)
        return application.getString(R.string.chose_restaurant_cuisine)
            .format(user.firstName, restaurant.cuisine, restaurant.name)
    }
}


