package com.cleanup.go4lunch.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.exhaustive
import com.cleanup.go4lunch.ui.detail.DetailsActivity
import com.cleanup.go4lunch.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    DetailsActivityLauncher {
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 44
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navBar: BottomNavigationView
    private lateinit var viewPager: ViewPager2
    private val viewModel: MainViewModel by viewModels()
    private var goingAtNoon: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navBar = findViewById(R.id.bottom_nav_view)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_the_drawer,
            R.string.close_the_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.setScrimColor(ContextCompat.getColor(this, android.R.color.transparent))
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        val headerView = findViewById<NavigationView>(R.id.side_nav).getHeaderView(0)

        viewModel.viewStateFlow.observe(this) {
            if (it.avatarUrl != null) Glide.with(baseContext).load(it.avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(headerView.findViewById(R.id.drawer_avatar))
            headerView.findViewById<TextView>(R.id.drawer_user_name).text = it.name
            headerView.findViewById<TextView>(R.id.drawer_user_email).text = it.connectedVia
            goingAtNoon = it.goingAtNoon
        }

        viewModel.viewActionSingleLiveEvent.observe(this) {
            when (it) {
                is MainViewAction.LaunchDetail -> startActivity(
                        DetailsActivity.navigate(this, it.osmId)
                    )
                is MainViewAction.StartNavNum -> viewPager.currentItem = it.number
            }.exhaustive
        }

        findViewById<NavigationView>(R.id.side_nav).setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> viewModel.onLogoutClicked()
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.your_lunch -> viewModel.onLunchClicked()
                else -> Unit
            }.exhaustive
            true
        }

        supportActionBar?.setHomeButtonEnabled(true)  // not setDisplayHomeAsUpEnabled(true)

        // todo spindle search:
        //  can't do filter-as-you-type + one dropdown element "enter to search online"
        //  as autocomplete is in requirements

        // supportFragmentManager retainedFragments is incompatible with Hilt.
        viewPager = findViewById(R.id.view_pager)

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        findViewById<TouchEventInterceptor>(R.id.touch_interceptor_view_group).viewPager = viewPager

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setTitle(if (position == 2) R.string.title_workmates else R.string.title_hungry)
                navBar.menu.findItem(
                    when (position) {
                        0 -> R.id.nav_map
                        1 -> R.id.nav_list
                        else -> R.id.nav_mates
                    }.exhaustive
                ).isChecked = true
            }
        })

        navBar.setOnItemSelectedListener {
            viewPager.currentItem = when (it.itemId) {
                R.id.nav_map -> 0
                R.id.nav_list -> 1
                else -> 2
            }.exhaustive
            true
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onStart() {
        requestPermissionsIfNecessary()
        viewModel.onStart()
        super.onStart()
    }

    override fun onStop() {
        viewModel.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.onDestroy(viewPager.currentItem)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        granted: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, granted)
        viewModel.permissionsUpdated(
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED,
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    private fun requestPermissionsIfNecessary() {
        val permissionsToRequest =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
                // other permissions from manifest are not stated dangerous
            ).filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
        if (permissionsToRequest.isNotEmpty()) ActivityCompat.requestPermissions(
            this, permissionsToRequest, PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onClicked(osmId: Long) {
        startActivity(DetailsActivity.navigate(this, osmId))
    }
}


