package com.cleanup.go4lunch.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.UseCase
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiMapperDelegate: PoiMapperDelegate,
    private val useCase: UseCase
) : ViewModel() {

    private val poiEntityStateFlow = MutableStateFlow<PoiEntity?>(null)

    fun onCreate(osmId: Long) {
        // todo inject Dispatchers.IO
        // todo Nino why not synchronous?
        viewModelScope.launch(Dispatchers.IO) {
            poiEntityStateFlow.tryEmit(useCase.getPoiById(osmId))
        }
    }

    val viewStateLiveData: LiveData<DetailsViewState> =
        combine(poiEntityStateFlow.filterNotNull(), useCase.sessionUserFlow) { poi, session ->
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

    private fun getNeighbourList(osmId: Long): List<DetailsViewState.Item> {
        return useCase.usersGoingThere(osmId).map {
            DetailsViewState.Item(
                mateId = it.id,
                imageUrl = it.avatarUrl ?: "",
                text = it.firstName
            )
        }
    }

}

