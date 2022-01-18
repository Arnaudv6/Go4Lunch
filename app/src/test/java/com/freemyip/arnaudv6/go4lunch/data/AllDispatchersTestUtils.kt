package com.freemyip.arnaudv6.go4lunch.data

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
fun getAllDispatchersForTesting(testCoroutineDispatcher: TestCoroutineDispatcher): AllDispatchers =
    mockk {
        every { mainDispatcher } returns testCoroutineDispatcher
        every { ioDispatcher } returns testCoroutineDispatcher
    }