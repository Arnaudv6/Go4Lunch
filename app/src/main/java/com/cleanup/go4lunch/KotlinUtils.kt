package com.cleanup.go4lunch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

inline fun <T> Flow<T>.collectWithLifecycle(
    owner: LifecycleOwner,
    crossinline action: suspend (value: T) -> Unit
) = owner.lifecycleScope.launch {
    owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        collect {
            action(it)
        }
    }

}

// todo generig debounce. Problem is with mutliple parrallel debouncers...
// https://gist.github.com/faruktoptas/c45272047fae8da61acfb7b14c451793

