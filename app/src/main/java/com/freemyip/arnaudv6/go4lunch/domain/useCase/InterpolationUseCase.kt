package com.freemyip.arnaudv6.go4lunch.domain.useCase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freemyip.arnaudv6.go4lunch.data.AllDispatchers
import com.freemyip.arnaudv6.go4lunch.data.users.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
abstract class InterpolationUseCase<T>(
    private val coroutineScope: CoroutineScope,
    private val dispatchers: AllDispatchers,
    private val timeout: Long = 1_000
) {
    private val interpolatedValueMutableLiveData = MutableLiveData<T?>()
    val liveData: LiveData<T?> = interpolatedValueMutableLiveData

    protected suspend fun interpolate(value: T, block: suspend () -> Unit) {
        withContext(dispatchers.mainDispatcher) {
            interpolatedValueMutableLiveData.value = value
        }

        val job = coroutineScope.launch(dispatchers.ioDispatcher) {
            delay(timeout)
            withContext(dispatchers.mainDispatcher) {
                interpolatedValueMutableLiveData.value = null
            }
        }

        block()

        job.cancel()
    }
}

