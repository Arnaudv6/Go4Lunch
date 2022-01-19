package com.freemyip.arnaudv6.go4lunch.domain.useCase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// TODO Nino découper : une seule méthode "invoke / execute" avec un nom de class "métier"
//  make use of GetSessionUserUseCase

class InterpolationUseCase @Inject constructor() {
    private val interpolatedValuesMutableLiveData = MutableLiveData(Values(null, null))
    val interpolatedValuesLiveData: LiveData<Values> = interpolatedValuesMutableLiveData

    fun setGoingAtNoon(goingAtNoon: Boolean?) {
        interpolatedValuesMutableLiveData.postValue(
            Values(goingAtNoon, interpolatedValuesLiveData.value?.isLikedPlace)
        )
    }

    fun setLikeCurrentPlace(isLikedPlace: Boolean?) {  // change boolean for a tmp likedPlacesList?
        interpolatedValuesMutableLiveData.postValue(
            Values(interpolatedValuesLiveData.value?.goingAtNoon, isLikedPlace)
        )
    }

    data class Values(val goingAtNoon: Boolean?, val isLikedPlace: Boolean?)
}

