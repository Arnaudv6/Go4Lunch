package com.cleanup.go4lunch.data.users

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class UsersRepository @Inject constructor() {

    fun usersGoing(placeId: Long): List<String> {
        return if (placeId % 2 == 0L) listOf("Kevin", "Sasha") else emptyList()
    }

    fun likes(restaurantId: Long): Int {
        return Random.nextInt(3)
        // todo
        //  Nino : il faut que les avis viennent du web ? google ? on c'est des avis in-house?
        //  make this code relevant
        //  notes, c'est celles de google
    }

}
