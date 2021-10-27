package com.cleanup.go4lunch.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cleanup.go4lunch.exhaustive
import com.cleanup.go4lunch.ui.list.PlacesListFragment
import com.cleanup.go4lunch.ui.map.MapFragment
import com.cleanup.go4lunch.ui.mates.MatesFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class MainPagerAdapter(
    fragmentActivity: FragmentActivity
) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MapFragment.newInstance()
            1 -> PlacesListFragment.newInstance()
            else -> MatesFragment.newInstance()
        }.exhaustive
    }
}
