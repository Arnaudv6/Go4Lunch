package com.cleanup.go4lunch.ui.utils

import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cleanup.go4lunch.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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


fun mySnackBar(message: String, coordinatorLayout: CoordinatorLayout) {
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).apply {
        (this.view.layoutParams as CoordinatorLayout.LayoutParams).gravity = Gravity.TOP
        this.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    }.setAction(coordinatorLayout.context.getString(R.string.dismiss)) {
        /* empty action: dismiss */
    }.show()
}

// write a good generic debounce. bellow code is crappy and fails with multiple parallel de-bouncers...
// https://gist.github.com/faruktoptas/c45272047fae8da61acfb7b14c451793

