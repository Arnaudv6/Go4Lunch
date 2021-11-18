package com.cleanup.go4lunch.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2

class TouchEventInterceptor @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var viewPager: ViewPager2? = null

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            viewPager?.isUserInputEnabled = viewPager?.currentItem != 0 || event.x > 0.93 * width
        }
        return false
    }

}

/* this class is made necessary as:
ViewPager2 is final (ViewPager was not): we can't override its methods.
It creates a child, viewPager[0], which is a recyclerview.

viewPager.setOnTouchListener does not get events
can't use a TouchDelegate
can add addOnItemTouchListener() but the I am not the only listener, and can't de-activate others
    short of accessing private fields https://stackoverflow.com/a/68785903
    (actually use a RecyclerView.SimpleOnItemTouchListener object as an argument then
    and override onInterceptTouchEvent())

can't play with requestDisallowInterceptTouchEvent() / onRequestDisallowInterceptTouchEvent() on children only
 */
