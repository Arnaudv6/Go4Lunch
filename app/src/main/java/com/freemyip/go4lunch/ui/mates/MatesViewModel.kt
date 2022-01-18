package com.freemyip.go4lunch.ui.mates

import android.app.Application
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.freemyip.go4lunch.R
import com.freemyip.go4lunch.data.SearchRepository
import com.freemyip.go4lunch.data.pois.PoiEntity
import com.freemyip.go4lunch.data.pois.PoiRepository
import com.freemyip.go4lunch.data.users.User
import com.freemyip.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class MatesViewModel @Inject constructor(
    private val application: Application,  // appContext notation gives a harmless "leak" linter warning.
    poiRepository: PoiRepository,
    searchRepository: SearchRepository,
    private val usersRepository: UsersRepository,
) : ViewModel() {

    private val greyColor = ContextCompat.getColor(application, R.color.grey)

    suspend fun swipeRefresh() = usersRepository.updateMatesList()

    // don't filter sessionUser out (nor in detailsVM) as list would refresh when not networkIsAvailable
    private val unfilteredMatesList = combine(
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
    }.filterNotNull()

    val mMatesListLiveData: LiveData<List<MatesViewStateItem>> = combine(
        unfilteredMatesList,
        searchRepository.searchStateFlow,
    ) { viewState, terms ->
        if (terms.isNullOrEmpty()) viewState
        else viewState.filter { it.text.contains(terms, ignoreCase = true) }
    }.asLiveData()

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


