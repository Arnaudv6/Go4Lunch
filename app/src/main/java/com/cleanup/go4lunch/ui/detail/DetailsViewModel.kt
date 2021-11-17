package com.cleanup.go4lunch.ui.detail

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import com.cleanup.go4lunch.data.useCase.UseCase
import com.cleanup.go4lunch.data.users.UsersRepository
import com.cleanup.go4lunch.exhaustive
import com.cleanup.go4lunch.ui.PoiMapperDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val poiRepository: PoiRepository,
    private val usersRepository: UsersRepository,
    private val poiMapperDelegate: PoiMapperDelegate,
    private val useCase: UseCase,
    private val sessionUserUseCase: SessionUserUseCase,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val colorActive = ContextCompat.getColor(appContext, R.color.orange)
    private val colorInactive = ContextCompat.getColor(appContext, R.color.grey)
    private val colorGold = ContextCompat.getColor(appContext, R.color.gold)

    private val poiEntityFlow: Flow<PoiEntity> = flow {
        val id = savedStateHandle.get<Long>(DetailsActivity.OSM_ID)
        // assert() only crashes at unit tests, not in release. Better use check().
        check(id != null) { "OSM_ID is not provided in the SavedStateHandle" }
        poiRepository.getPoiById(id)?.let { emit(it) }
    }

    private val colleaguesListFlow: Flow<List<DetailsViewState.Item>> = poiEntityFlow.map {
        useCase.usersGoingThere(it.id).map { user ->
            DetailsViewState.Item(
                mateId = user.id,
                imageUrl = user.avatarUrl ?: "",
                text = user.firstName
            )
        }
    } // todo c'est là que j'avais mis du state in alors qu'il faut du flow au repo.

    val viewStateLiveData: LiveData<DetailsViewState> =
        combine(
            poiEntityFlow,
            sessionUserUseCase.sessionUserFlow,
            colleaguesListFlow,
        ) { poi, session, colleagues ->
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

    fun goingAtNoonClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = sessionUserUseCase.sessionUserFlow.first()?.user?.id
            val placeId = savedStateHandle.get<Long>(DetailsActivity.OSM_ID)
            if (userId != null && placeId != null) usersRepository.setGoingAtNoon(userId, placeId)
        }
        // todo interpolation
    }

    fun likeClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = sessionUserUseCase.sessionUserFlow.first()?.user?.id
            val placeId = savedStateHandle.get<Long>(DetailsActivity.OSM_ID)
            if (userId != null && placeId != null) {
                when (sessionUserUseCase.sessionUserFlow.first()?.liked?.contains(placeId)) {
                    true -> usersRepository.deleteLiked(userId, placeId)
                    false -> usersRepository.insertLiked(userId, placeId)
                    null -> Unit
                }.exhaustive // todo Nino : c'est un cas où exhaustive marche bof, non?
            }
        }
        // todo interpolation
    }


}

