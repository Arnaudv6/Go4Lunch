package com.freemyip.arnaudv6.go4lunch.data

import kotlinx.coroutines.CoroutineDispatcher

data class AllDispatchers(
    val mainDispatcher: CoroutineDispatcher,
    val ioDispatcher: CoroutineDispatcher
)


