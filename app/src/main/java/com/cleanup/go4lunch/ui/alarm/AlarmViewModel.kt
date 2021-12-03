package com.cleanup.go4lunch.ui.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.useCase.SessionUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    sessionUserUseCase: SessionUserUseCase,
    poiRepository: PoiRepository,
) : ViewModel() {

    val lunchPlaceLiveData: LiveData<PoiEntity?> = combine(
        sessionUserUseCase.sessionUserFlow.filterNotNull(),
        poiRepository.cachedPOIsListFlow.filterNotNull()
    ) { session, list ->
        session.user.goingAtNoon?.let { list.firstOrNull { poi -> poi.id == it } }
    }.asLiveData()

}

