package com.cleanup.go4lunch.ui.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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


/** used like so: when{}.exhaustive
 *  then when statements, (used to collect viewActions, for example).
 *  show not only a warning but a compile-time error if some branches are not taken care of.
 */
val <T> T.exhaustive: T
    get() = this


// write a good generic debounce. bellow code is crappy and fails with multiple parallel de-bouncers...
// https://gist.github.com/faruktoptas/c45272047fae8da61acfb7b14c451793

