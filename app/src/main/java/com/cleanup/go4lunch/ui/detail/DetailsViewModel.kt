package com.cleanup.go4lunch.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.data.UseCase
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import com.cleanup.go4lunch.ui.mates.MatesViewStateItem
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
    private val poiMapperDelegate: PoiMapperDelegate,
    private val useCase: UseCase
) : ViewModel() {

    private val placeIdMutableStateFlow = MutableStateFlow<Long?>(null)

    private val placePoiEntityFlow = placeIdMutableStateFlow
        .filterNotNull().map { useCase.getPoiById(it) }.filterNotNull()

    fun onCreate(osmId: Long) {
        placeIdMutableStateFlow.tryEmit(osmId)
    }

    val viewStateLiveData: LiveData<DetailsViewState> =
        combine(placePoiEntityFlow, useCase.sessionUserFlow) { poi, session ->
            DetailsViewState(
                name = poi.name,
                goAtNoon = session?.user?.goingAtNoon == poi.id,
                rating = poi.rating,
                address = poiMapperDelegate.cuisineAndAddress(poi.cuisine, poi.address),
                bigImageUrl = poi.imageUrl.removeSuffix("/preview"),
                call = poi.phone,
                callActive = true, // poi.phone.isNotEmpty(),
                likeActive = true, // usersRepository.toggleLiked(user.id, poi.id),
                website = "", // poi.site,
                websiteActive = true, //poi.site.isNotEmpty(),
                neighbourList = getNeighbourList(poi.id)
            )
        }.asLiveData()

    private fun getNeighbourList(osmId: Long): List<MatesViewStateItem> {
        return useCase.usersGoingThere(osmId).map {
            MatesViewStateItem(
                id = it.id,
                imageUrl = it.avatarUrl ?: "",
                text = it.firstName
            )
        }
    }

}

