package com.freemyip.arnaudv6.go4lunch.ui.detail

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.data.ConnectivityRepository
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiEntity
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiMapperDelegate
import com.freemyip.arnaudv6.go4lunch.data.pois.PoiRepository
import com.freemyip.arnaudv6.go4lunch.data.users.User
import com.freemyip.arnaudv6.go4lunch.data.users.UsersRepository
import com.freemyip.arnaudv6.go4lunch.domain.useCase.GetMatesByPlaceUseCase
import com.freemyip.arnaudv6.go4lunch.domain.useCase.InterpolationUseCase
import com.freemyip.arnaudv6.go4lunch.domain.useCase.GetSynchronizedUserUseCase
import com.freemyip.arnaudv6.go4lunch.ui.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val poiMapperDelegate: PoiMapperDelegate,
    getSynchronizedUserUseCase: GetSynchronizedUserUseCase,
    getMatesByPlaceUseCase: GetMatesByPlaceUseCase,
    private val savedStateHandle: SavedStateHandle, //
    private val interpolationUseCase: InterpolationUseCase,
    private val allDispatchers: AllDispatchers,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val colorActive = ContextCompat.getColor(appContext, R.color.orange)
    private val colorInactive = ContextCompat.getColor(appContext, R.color.grey)
    private val colorGold = ContextCompat.getColor(appContext, R.color.gold)

    val intentSingleLiveEvent = SingleLiveEvent<DetailsViewAction>()

    init {
        // same code as in MainViewModel, applies only when launched from notification.
        viewModelScope.launch(allDispatchers.ioDispatcher) {
            connectivityRepository.isNetworkAvailableFlow.collect {
                if (it) usersRepository.updateMatesList()
            }
        }
    }

    private val osmIdLiveData = savedStateHandle.getLiveData<Long?>(DetailsActivity.OSM_ID)

    private val matesByPlaceLivedata = getMatesByPlaceUseCase().asLiveData()

    private val sessionUserLiveData = getSynchronizedUserUseCase().asLiveData()

    private val ratingsLiveData = usersRepository.placesRatingsFlow.asLiveData()

    private val poiLiveData: LiveData<PoiEntity?> = osmIdLiveData.switchMap {
        liveData { emit(if (it == null) null else poiRepository.getPoiById(it)) }
    }

    private val goingMatesListLiveData = MediatorLiveData<List<DetailsViewState.Item>>().apply {
        addSource(osmIdLiveData) { osmId -> value = goingMates(osmId, matesByPlaceLivedata.value) }
        addSource(matesByPlaceLivedata) { mates -> value = goingMates(osmIdLiveData.value, mates) }
    }

    private fun goingMates(
        osmId: Long?,
        matesByPlace: Map<Long, ArrayList<User>>?
    ): List<DetailsViewState.Item> = matesByPlace?.get(osmId)?.map { user ->
        DetailsViewState.Item(
            mateId = user.email,
            imageUrl = user.avatarUrl.orEmpty(),
            text = user.firstName
        )
    } ?: emptyList()

    val viewStateLiveData: LiveData<DetailsViewState> = MediatorLiveData<DetailsViewState>().apply {
        addSource(poiLiveData) { poi ->
            toViewState(
                poi,
                sessionUserLiveData.value,
                goingMatesListLiveData.value,
                interpolationUseCase.interpolatedValuesLiveData.value,
                ratingsLiveData.value
            )?.let { value = it }
        }
        addSource(sessionUserLiveData) { session ->
            toViewState(
                poiLiveData.value,
                session,
                goingMatesListLiveData.value,
                interpolationUseCase.interpolatedValuesLiveData.value,
                ratingsLiveData.value
            )?.let { value = it }
        }
        addSource(goingMatesListLiveData) { mates ->
            toViewState(
                poiLiveData.value,
                sessionUserLiveData.value,
                mates,
                interpolationUseCase.interpolatedValuesLiveData.value,
                ratingsLiveData.value
            )?.let { value = it }
        }
        addSource(interpolationUseCase.interpolatedValuesLiveData) { interpolated ->
            toViewState(
                poiLiveData.value,
                sessionUserLiveData.value,
                goingMatesListLiveData.value,
                interpolated,
                ratingsLiveData.value
            )?.let { value = it }
        }
        addSource(ratingsLiveData) { ratings ->
            toViewState(
                poiLiveData.value,
                sessionUserLiveData.value,
                goingMatesListLiveData.value,
                interpolationUseCase.interpolatedValuesLiveData.value,
                ratings
            )?.let { value = it }
        }
    }

    private fun toViewState(
        poiEntity: PoiEntity?,
        session: User?,
        mates: List<DetailsViewState.Item>?,
        interpolatedValues: InterpolationUseCase.Values?,
        ratings: Map<Long, Int>?
    ): DetailsViewState? {
        poiEntity?.let { poi ->
            val goingColor =
                interpolatedValues?.goingAtNoon?.let { if (it) colorGold else colorInactive }
                    ?: if (session?.goingAtNoon == poi.id) colorGold else colorInactive
            val likeColor =
                interpolatedValues?.isLikedPlace?.let { if (it) colorActive else colorInactive }
                    ?: if (session?.liked?.contains(poi.id) == true) colorActive else colorInactive
            return DetailsViewState(
                name = poi.name,
                goAtNoonColor = goingColor,
                rating = ratings?.get(poi.id)?.toFloat(),
                address = poiMapperDelegate.cuisineAndAddress(poi.cuisine, poi.address),
                bigImageUrl = poi.imageUrl.removeSuffix("/preview"),
                callColor = if (poi.phone.isNullOrEmpty()) colorInactive else colorActive,
                callActive = !poi.phone.isNullOrEmpty(),
                likeColor = likeColor,
                likeActive = session?.liked?.contains(poi.id) ?: false,
                websiteColor = if (poi.site.isNullOrEmpty()) colorInactive else colorActive,
                websiteActive = !poi.site.isNullOrEmpty(),
                matesList = mates ?: emptyList()
            )
        }
        return null
    }

    fun goingAtNoonClicked() {
        savedStateHandle.get<Long>(DetailsActivity.OSM_ID)?.let { placeId ->
            viewModelScope.launch(allDispatchers.ioDispatcher) {
                sessionUserLiveData.value?.let { user ->
                    val initialState = user.goingAtNoon == placeId
                    interpolationUseCase.setGoingAtNoon(user, placeId, !initialState)
                }
            }
        }
    }

    fun likeClicked() {
        savedStateHandle.get<Long>(DetailsActivity.OSM_ID)?.let { placeId ->
            viewModelScope.launch(allDispatchers.ioDispatcher) {
                sessionUserLiveData.value?.let { session ->
                    session.liked?.let { liked ->
                        val initialState = liked.contains(placeId)
                        interpolationUseCase.setLikeCurrentPlace(!initialState)
                        launch {
                            delay(1_000)
                            interpolationUseCase.setLikeCurrentPlace(null)
                        }
                        if (initialState) usersRepository.deleteLiked(session.email, placeId)
                        else usersRepository.insertLiked(session.email, placeId)

                    }
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

