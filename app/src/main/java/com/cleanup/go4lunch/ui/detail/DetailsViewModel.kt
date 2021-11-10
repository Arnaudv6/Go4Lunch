package com.cleanup.go4lunch.ui.detail

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.UseCase
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiRepository: PoiRepository,
    private val poiMapperDelegate: PoiMapperDelegate,
    private val useCase: UseCase,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val colorActive = ContextCompat.getColor(appContext, R.color.orange)
    private val colorInactive = ContextCompat.getColor(appContext, R.color.grey)
    private val colorGold = ContextCompat.getColor(appContext, R.color.gold)

    val viewStateLiveData: LiveData<DetailsViewState> =
        combine(
            flow {
                val id = savedStateHandle.get<Long>(DetailsActivity.OSM_ID)
                if (id != null) { // todo Nino : là, je serais tenté de mettre assert()?
                    val poi = poiRepository.getPoiById(id)
                    if (poi != null) emit(poi)
                }
            },
            useCase.sessionUserFlow
        ) { poi, session ->
            DetailsViewState(
                name = poi.name,
                goAtNoonColor = if (session?.user?.goingAtNoon == poi.id) colorGold else colorInactive,
                rating = poi.rating,
                address = poiMapperDelegate.cuisineAndAddress(poi.cuisine, poi.address),
                bigImageUrl = poi.imageUrl.removeSuffix("/preview"),
                call = poi.phone,
                callColor = if (poi.phone.isNullOrEmpty()) colorInactive else colorActive,
                callActive = !poi.phone.isNullOrEmpty(),
                likeColor = if (session?.liked?.contains(poi.id) == false) colorInactive else colorActive,
                likeActive = session?.liked?.contains(poi.id) ?: false,
                website = poi.site.orEmpty(),
                websiteColor = if (poi.site.isNullOrEmpty()) colorInactive else colorActive,
                websiteActive = !poi.site.isNullOrEmpty(),
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

    fun goingAtNoonClicked() {
        // todo interpolation
    }

}

