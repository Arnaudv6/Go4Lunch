package com.cleanup.go4lunch.data

import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

// todo Nino : comme ça ?
@Singleton
data class AllDispatchers @Inject constructor(
    val mainDispatcher: CoroutineDispatcher,
    val ioDispatcher: CoroutineDispatcher
)


