package com.cleanup.go4lunch.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.users.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val viewStateMutableLiveData = MutableLiveData<DetailsViewState>()
    val viewStateLiveData: LiveData<DetailsViewState> = viewStateMutableLiveData

    fun getViewState(osmId: Long) {

        viewModelScope.launch {
            val poi = poiRepository.getPoiById(osmId) ?: return@launch
            viewStateMutableLiveData.value = DetailsViewState(
                name = poi.name,
                goAtNoon = usersRepository.goingAtNoon() == poi.id,
                likes = usersRepository.likes(poi.id),
                address = poi.cuisineAndAddress(),
                bigImageUrl = poi.imageUrl.removeSuffix("/preview"),
                call = poi.phone,
                callActive = poi.phone.isNotEmpty(),
                likeActive = usersRepository.like(poi.id),
                website = poi.site,
                websiteActive = poi.site.isNotEmpty(),
                neighbourList = getNeighbourList(poi.id)
            )
        }
    }

    private fun getNeighbourList(osmId: Long): List<DetailsViewState.Neighbour> {
        return usersRepository.usersGoing(osmId).map {
            DetailsViewState.Neighbour(it.avatarUrl, it.firstName)
        }
    }
}

