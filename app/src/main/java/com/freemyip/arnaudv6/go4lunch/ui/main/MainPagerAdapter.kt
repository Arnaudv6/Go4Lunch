package com.freemyip.arnaudv6.go4lunch.ui.main

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freemyip.arnaudv6.go4lunch.ui.utils.exhaustive
import com.freemyip.arnaudv6.go4lunch.ui.list.PlacesListFragment
import com.freemyip.arnaudv6.go4lunch.ui.map.MapFragment
import com.freemyip.arnaudv6.go4lunch.ui.mates.MatesFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int) = when (position) {
        0 -> MapFragment.newInstance()
        1 -> PlacesListFragment.newInstance()
        else -> MatesFragment.newInstance()
    }.exhaustive
}
