package com.cleanup.go4lunch.data.users

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class UsersRepository @Inject constructor() {

    fun usersGoing(osmId: Long): List<User> {
        return if (osmId % 2 == 0L) listOf(
            User(1, "Kevin","McNolan","",135465),
            User(2,"Sasha","VanViktor","",952473)
            ) else emptyList()
    }

    fun likes(restaurantId: Long): Int {
        return Random.nextInt(3)
        // todo
        //  Nino : il faut que les avis viennent du web ? google ? on c'est des avis in-house?
        //  make this code relevant
        //  notes, c'est celles de google
    }

    fun goingAtNoon(): Long = 135423

    fun like(osmId: Long): Boolean = osmId % 2 == 0L

}
