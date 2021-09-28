package com.cleanup.go4lunch.repository

import android.location.Location
import android.util.Log
import com.cleanup.go4lunch.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// todo understand: @Inject constructor() is mandatory ever since I annotated @Singleton
class Repository @Inject constructor() {


    private val string = "" +
            "users:" +
            "   avatar" +
            "   first name" +
            "   last name" +
            "   liste de restaus en favoris" +
            "   restau choisi pour midi (MàJ à 16h?)" +
            "" +
            "" +
            "" +
            "restaus:" +
            "   nom" +
            "   adresse" +
            "   coordonnées GPS (pour calculer des distances)" +
            "   image (si dispo)" +
            "   type de restau (optionnel)" +
            "   horaires" +
            "   numéro" +
            "   site" +
            "" +
            "" +
            "" +
            "mises à jour responsive:" +
            "   compte de l'utilisateur" +
            "   coordonées GPS" +
            "   notes des restaus : utilisateurs qui l'ont en favori / utilisateurs ayant été * 3" +
            "   distances des restaus" +
            "   couleur des restaus (target ou non)" +
            "" +
            "" +
            "" +
            "valider un déjeuner quand l'uilisateur se trouve entre 11h30 et 14h à 10m du restau qu'il a annoncé" +
            "   ajouter le restau dans la liste visitée de l'user" +
            "   lui proposer de le mettre en favori" +
            "   virer son intention" +
            ""

    private val loc = Location("repository")
    private val locationFlow = MutableStateFlow(loc)
    private val poiProvider = NominatimPOIProvider(BuildConfig.APPLICATION_ID)
    private val pointsOfInterest: Flow<ArrayList<POI>>

    init {
        loc.latitude = 48.8583  // starting Location: Eiffel Tower
        loc.longitude = 2.2944
        locationFlow.value = loc

        pointsOfInterest = locationFlow.mapNotNull { poiLoc: Location ->
            (
                    when (poiLoc) {
                        loc -> null
                        else -> poiProvider.getPOICloseTo(GeoPoint(poiLoc), "restaurant", 50, 0.025)
                    }
                    )
        }
    }

    fun setLocation(location: Location) {
        // emit() requires "suspend"
        locationFlow.value = location
        Log.e("Repository", "setLocation() called with: ${location.toString()}")
    }

    fun getLocationFlow(): Flow<Location> {
        return locationFlow
    }

    fun getPointsOfInterest(): Flow<ArrayList<POI>> {
        return pointsOfInterest
    }


}


