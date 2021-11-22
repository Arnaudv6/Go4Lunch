package com.cleanup.go4lunch.ui.detail

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.session.SessionUser
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import com.cleanup.go4lunch.ui.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
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
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val colorActive = ContextCompat.getColor(appContext, R.color.orange)
    private val colorInactive = ContextCompat.getColor(appContext, R.color.grey)
    private val colorGold = ContextCompat.getColor(appContext, R.color.gold)

    val intentSingleLiveEvent = SingleLiveEvent<DetailsViewAction>()

    private val idFlow = savedStateHandle.getLiveData<Long?>(DetailsActivity.OSM_ID).asFlow()

    private val poiEntityFlow: Flow<PoiEntity> = idFlow.mapNotNull { poiRepository.getPoiById(it) }

    private var session: SessionUser? = null
    private var poi: PoiEntity? = null

    private val colleaguesListFlow: Flow<List<DetailsViewState.Item>> =
        combine(idFlow, usersRepository.matesListFlow) { id, mates ->
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
            poiEntityFlow,
            sessionUserUseCase.sessionUserFlow,
            colleaguesListFlow,
            interpolatedColleagues
        ) { poi, session, colleagues, interpolatedState ->
            this.session = session // todo Nino : je peux sauver ca dans un field?
            this.poi = poi
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
                colleaguesList = colleagues
            )
        }.asLiveData()

    // todo snackBar if user clicks between 14h30 and 24h?
    fun goingAtNoonClicked() {
        poi?.id?.let { placeId -> // 'if' would not fix the complex value: linter would complain
            session?.user?.let {
                viewModelScope.launch(Dispatchers.IO) {
                    interpolatedColleagues.value = true
                    launch {
                        delay(1_000)
                        interpolatedColleagues.value = false
                    }
                    if (it.goingAtNoon == placeId) usersRepository.setGoingAtNoon(it.id, null)
                    else usersRepository.setGoingAtNoon(it.id, placeId)
                }
            }
        }
    }

    fun likeClicked(lol: String) {
        poi?.id?.let { placeId ->
            session?.let {
                viewModelScope.launch(Dispatchers.IO) {
                    if (it.liked.contains(placeId)) usersRepository.deleteLiked(it.user.id, placeId)
                    else usersRepository.insertLiked(it.user.id, placeId)
                }
            }
        }  // todo interpolation
    }

    fun callClicked() = poi?.phone?.let { intentSingleLiveEvent.value = DetailsViewAction.Call(it) }

    fun webClicked() = poi?.site?.let { intentSingleLiveEvent.value = DetailsViewAction.Surf(it) }
}

