package com.cleanup.go4lunch.ui.detail

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.session.SessionUser
import com.cleanup.go4lunch.data.useCase.InterpolationUseCase
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
    private val poiMapperDelegate: PoiMapperDelegate,
    sessionUserUseCase: SessionUserUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val interpolationUseCase: InterpolationUseCase,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val colorActive = ContextCompat.getColor(appContext, R.color.orange)
    private val colorInactive = ContextCompat.getColor(appContext, R.color.grey)
    private val colorGold = ContextCompat.getColor(appContext, R.color.gold)

    val intentSingleLiveEvent = SingleLiveEvent<DetailsViewAction>()

    private val osmIdLiveData = savedStateHandle.getLiveData<Long?>(DetailsActivity.OSM_ID)

    private val matesListLiveData = usersRepository.matesListFlow.asLiveData()

    private val sessionUserLiveData = sessionUserUseCase.sessionUserFlow.asLiveData()

    private val poiLiveData: LiveData<PoiEntity?> = osmIdLiveData.switchMap {
        liveData { emit(if (it != null) poiRepository.getPoiById(it) else null) }
    }

    private val goingMatesListLiveData = MediatorLiveData<List<DetailsViewState.Item>>().apply {
        addSource(osmIdLiveData) { osmId -> value = goingMates(osmId, matesListLiveData.value) }
        addSource(matesListLiveData) { mates -> value = goingMates(osmIdLiveData.value, mates) }
    }

    private fun goingMates(osmId: Long?, mates: List<User>?): List<DetailsViewState.Item> =
        mates?.filter { user -> user.goingAtNoon == osmId }?.map { user ->
            DetailsViewState.Item(
                mateId = user.id,
                imageUrl = user.avatarUrl ?: "",
                text = user.firstName
            )
        } ?: emptyList()

    val viewStateLiveData: LiveData<DetailsViewState> = MediatorLiveData<DetailsViewState>().apply {
        addSource(poiLiveData) { poi ->
            toViewState(
                poi,
                sessionUserLiveData.value,
                goingMatesListLiveData.value,
                interpolationUseCase.interpolatedValuesLiveData.value
            )?.let { value = it }
        }
        addSource(sessionUserLiveData) { session ->
            toViewState(
                poiLiveData.value,
                session,
                goingMatesListLiveData.value,
                interpolationUseCase.interpolatedValuesLiveData.value
            )?.let { value = it }
        }
        addSource(goingMatesListLiveData) { mates ->
            toViewState(
                poiLiveData.value,
                sessionUserLiveData.value,
                mates,
                interpolationUseCase.interpolatedValuesLiveData.value
            )?.let { value = it }
        }
        addSource(interpolationUseCase.interpolatedValuesLiveData) { interpolated ->
            toViewState(
                poiLiveData.value,
                sessionUserLiveData.value,
                goingMatesListLiveData.value,
                interpolated
            )?.let { value = it }
        }
    }

    private fun toViewState(
        poiEntity: PoiEntity?,
        session: SessionUser?,
        colleagues: List<DetailsViewState.Item>?,
        interpolatedValues: InterpolationUseCase.Values?
    ): DetailsViewState? {
        poiEntity?.let { poi ->
            val goingColor =
                interpolatedValues?.goingAtNoon?.let { if (it) colorGold else colorInactive }
                    ?: if (session?.user?.goingAtNoon == poi.id) colorGold else colorInactive
            val likeColor =
                interpolatedValues?.isLikedPlace?.let { if (it) colorActive else colorInactive }
                    ?: if (session?.liked?.contains(poi.id) == true) colorActive else colorInactive
            return DetailsViewState(
                name = poi.name,
                goAtNoonColor = goingColor,
                rating = poi.rating,
                address = poiMapperDelegate.cuisineAndAddress(poi.cuisine, poi.address),
                bigImageUrl = poi.imageUrl.removeSuffix("/preview"),
                callColor = if (poi.phone.isNullOrEmpty()) colorInactive else colorActive,
                callActive = !poi.phone.isNullOrEmpty(),
                likeColor = likeColor,
                likeActive = session?.liked?.contains(poi.id) ?: false,
                websiteColor = if (poi.site.isNullOrEmpty()) colorInactive else colorActive,
                websiteActive = !poi.site.isNullOrEmpty(),
                colleaguesList = colleagues ?: emptyList()
            )
        }
        return null
    }

    fun goingAtNoonClicked() {
        savedStateHandle.get<Long>(DetailsActivity.OSM_ID)?.let { placeId ->
            viewModelScope.launch(Dispatchers.IO) {
                sessionUserLiveData.value?.user?.let { user ->
                    val initialState = user.goingAtNoon == placeId
                    interpolationUseCase.setGoingAtNoon(!initialState)
                    launch {
                        delay(1_000)
                        interpolationUseCase.setGoingAtNoon(null)
                    }
                    if (initialState) usersRepository.setGoingAtNoon(user.id, null)
                    else usersRepository.setGoingAtNoon(user.id, placeId)
                }
            }
        }
    }

    fun likeClicked() {
        savedStateHandle.get<Long>(DetailsActivity.OSM_ID)?.let { placeId ->
            viewModelScope.launch(Dispatchers.IO) {
                sessionUserLiveData.value?.let { session ->
                    val initialState = session.liked.contains(placeId)
                    interpolationUseCase.setLikeCurrentPlace(!initialState)
                    launch {
                        delay(1_000)
                        interpolationUseCase.setLikeCurrentPlace(null)
                    }
                    if (initialState) usersRepository.deleteLiked(session.user.id, placeId)
                    else usersRepository.insertLiked(session.user.id, placeId)
                }
            }
        }
    }

    fun callClicked() {
        poiLiveData.value?.phone?.let { intentSingleLiveEvent.value = DetailsViewAction.Call(it) }
    }

    fun webClicked() {
        poiLiveData.value?.site?.let { intentSingleLiveEvent.value = DetailsViewAction.Surf(it) }
    }
}

