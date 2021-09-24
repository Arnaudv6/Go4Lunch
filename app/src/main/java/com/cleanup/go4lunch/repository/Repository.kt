package com.cleanup.go4lunch.repository

import android.location.Location
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor() {
    // todo understand: @Inject constructor() is mandatory ever since I annotated @Singleton

    private var location: Location = Location("repository")

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

    init {
        // starting Location: Eiffel Tower
        location.latitude = 48.8583
        location.longitude = 2.2944
    }

    fun setLocation(location: Location) {
        this.location = location
        Log.e("Arnaud", "Repo speaking: setLocation: ${location.toString()}")
    }


}


