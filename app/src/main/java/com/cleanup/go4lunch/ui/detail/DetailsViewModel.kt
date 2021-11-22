package com.cleanup.go4lunch.ui.detail

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
    private val poiMapperDelegate: PoiMapperDelegate,
    private val sessionUserUseCase: SessionUserUseCase,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val colorActive = ContextCompat.getColor(appContext, R.color.orange)
    private val colorInactive = ContextCompat.getColor(appContext, R.color.grey)
    private val colorGold = ContextCompat.getColor(appContext, R.color.gold)

    val intentSingleLiveEvent = SingleLiveEvent<DetailsViewAction>()

    private val osmIdFlow =
        savedStateHandle.getLiveData<Long?>(DetailsActivity.OSM_ID).asFlow() // todo livedata

    private val poiFlow: Flow<PoiEntity> = osmIdFlow.mapNotNull { poiRepository.getPoiById(it) }

    private val colleaguesListFlow: Flow<List<DetailsViewState.Item>> =
        combine(osmIdFlow, usersRepository.matesListFlow) { id, mates ->
            mates.filter { user -> user.goingAtNoon == id }.map { user ->
                DetailsViewState.Item(
                    mateId = user.id,
                    imageUrl = user.avatarUrl ?: "",
                    text = user.firstName
                )
            }
        }

    // TODO Arnaud usecase
    private val interpolatedColleagues = MutableStateFlow<Boolean?>(null)

    val viewStateLiveData: LiveData<DetailsViewState> =
        combine(
            poiFlow,
            sessionUserUseCase.sessionUserFlow,
            colleaguesListFlow,
            interpolatedColleagues
        ) { poi, session, colleagues, interpolatedState ->
            val color = interpolatedState?.let { if (it) colorGold else colorInactive }
                ?: if (session?.user?.goingAtNoon == poi.id) colorGold else colorInactive
            DetailsViewState(
                name = poi.name,
                goAtNoonColor = color,
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
                colleaguesList = colleagues
            )
        }.asLiveData()

    fun goingAtNoonClicked() {
        val placeId = savedStateHandle.get<Long>(DetailsActivity.OSM_ID)
        if (placeId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val session = sessionUserUseCase.sessionUserFlow.filterNotNull().firstOrNull()
                if (session != null) {
                    val initialState = session.user.goingAtNoon == placeId
                    interpolatedColleagues.value = !initialState
                    launch {
                        delay(1_000)
                        interpolatedColleagues.value = null
                    }
                    if (initialState) usersRepository.setGoingAtNoon(session.user.id, null)
                    else usersRepository.setGoingAtNoon(session.user.id, placeId)
                }
            }
        }
    }

    fun likeClicked() {
        val placeId = savedStateHandle.get<Long>(DetailsActivity.OSM_ID)
        if (placeId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val session = sessionUserUseCase.sessionUserFlow.filterNotNull().firstOrNull()
                if (session != null) {
                    if (session.liked.contains(placeId)) usersRepository.deleteLiked(
                        session.user.id,
                        placeId
                    )
                    else usersRepository.insertLiked(session.user.id, placeId)
                }
            }
        }  // todo interpolation
    }

    fun callClicked() {
        // todo pareil : filterNotNull().first()
        viewModelScope.launch(Dispatchers.IO) {
            poiFlow.filterNotNull().first().phone?.let {
                launch(Dispatchers.Main) {
                    intentSingleLiveEvent.value = DetailsViewAction.Call(it)
                }
            }
        }
    }

    fun webClicked() {
        // todo pareil : filterNotNull().first()
        viewModelScope.launch(Dispatchers.IO) {
            poiFlow.filterNotNull().first().site?.let {
                launch(Dispatchers.Main) {
                    intentSingleLiveEvent.value = DetailsViewAction.Surf(it)
                }
            }
        }
    }

}

