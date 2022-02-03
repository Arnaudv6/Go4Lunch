package com.freemyip.arnaudv6.go4lunch.domain.useCase

import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class InterpolationUseCase<T>(
    private val dispatchers: AllDispatchers,
    private val timeout: Long = 1_000
) {
    private val interpolatedValueMutableStateFlow = MutableStateFlow<T?>(null)
    val flow: Flow<T?> = interpolatedValueMutableStateFlow.asStateFlow()

    protected suspend fun interpolate(value: T, block: suspend () -> Unit) {
        withContext(dispatchers.mainDispatcher) {
            interpolatedValueMutableStateFlow.value = value
        }

        val job = withContext(dispatchers.ioDispatcher) {
            launch(dispatchers.ioDispatcher) {
                delay(timeout)
                withContext(dispatchers.mainDispatcher) {
                    interpolatedValueMutableStateFlow.value = null
                }
            }
        }

        block()

        job.cancel()
    }
}

