package com.cleanup.go4lunch.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import com.cleanup.go4lunch.ui.mates.Mate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
    private val poiMapperDelegate: PoiMapperDelegate
) : ViewModel() {

    private val placeIdMutableStateFlow = MutableStateFlow<Long?>(null)

    fun onCreate(osmId: Long) {
        placeIdMutableStateFlow.tryEmit(osmId)
    }

    val viewStateLiveData: LiveData<DetailsViewState> =
        placeIdMutableStateFlow.filterNotNull().map {
            poiRepository.getPoiById(it)
        }.filterNotNull().combine(usersRepository.sessionUserFlow.filterNotNull()) { poi, user ->
            DetailsViewState(
                name = poi.name,
                goAtNoon = user.goingAtNoon == poi.id,
                likes = usersRepository.likes(poi.id),
                address = poiMapperDelegate.cuisineAndAddress(poi.cuisine, poi.address),
                bigImageUrl = poi.imageUrl.removeSuffix("/preview"),
                call = poi.phone,
                callActive = poi.phone.isNotEmpty(),
                likeActive = usersRepository.like(poi.id),
                website = poi.site,
                websiteActive = poi.site.isNotEmpty(),
                neighbourList = getNeighbourList(poi.id)
            )
        }.asLiveData()

    private fun getNeighbourList(osmId: Long): List<Mate> {
        return usersRepository.usersGoingAtPlaceId(osmId).map {
            Mate(id = it.id, imageUrl = it.avatarUrl ?: "", text = it.firstName)
        }
    }

}

