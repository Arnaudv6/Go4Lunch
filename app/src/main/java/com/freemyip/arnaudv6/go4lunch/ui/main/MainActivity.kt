package com.freemyip.arnaudv6.go4lunch.ui.main

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.freemyip.arnaudv6.go4lunch.R
import com.freemyip.arnaudv6.go4lunch.ui.detail.DetailsActivity
import com.freemyip.arnaudv6.go4lunch.ui.settings.SettingsActivity
import com.freemyip.arnaudv6.go4lunch.ui.utils.exhaustive
import com.freemyip.arnaudv6.go4lunch.ui.utils.mySnackBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback,
    DetailsActivityLauncher {
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 44
        private const val LAST_VIEW_PAGER_ITEM = "LAST_VIEW_PAGER_ITEM"
    }

    private val viewModel: MainViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setAuthorizationResponse(intent)

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)  // not setDisplayHomeAsUpEnabled(true) for drawer

        drawerLayout = findViewById(R.id.drawer_layout)
        val navBar = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

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
        val avatar = headerView.findViewById<ImageView>(R.id.drawer_avatar)

        viewModel.viewStateFlow.observe(this) { viewState ->
            avatar.setImageDrawable(
                AppCompatResources.getDrawable(applicationContext, R.drawable.ic_baseline_group_24)
            )
            viewState.avatarUrl?.let {
                Glide.with(baseContext).load(it)
                    .apply(RequestOptions.circleCropTransform()).into(avatar)
            }
            headerView.findViewById<TextView>(R.id.drawer_user_name).text = viewState.name
            headerView.findViewById<TextView>(R.id.drawer_user_email).text = viewState.connectedVia
        }

        viewModel.viewActionSingleLiveEvent.observe(this) {
            when (it) {
                is MainViewAction.LaunchDetail ->
                    startActivity(DetailsActivity.navigate(this, it.osmId))
                is MainViewAction.SnackBar ->
                    mySnackBar(it.message, findViewById(R.id.snackbar_anchor_layout))
                is MainViewAction.InitAuthorization -> initAuthorization(it.authorizationRequest)
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

        // supportFragmentManager retainedFragments is incompatible with Hilt. ViewPager it is.
        viewPager = findViewById(R.id.view_pager)

        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        savedInstanceState?.getInt(LAST_VIEW_PAGER_ITEM, 0)?.let { viewPager.currentItem = it }
        // I used sharedPreferences before, from the repo... with @WorkerThread annotation
        // could not successfully override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {

        findViewById<TouchEventInterceptor>(R.id.touch_interceptor_view_group).viewPager = viewPager

        // we could add search spindle with autocomplete / type-ahead
        val searchView = findViewById<SearchView>(R.id.search_view)

        toolbar.setOnClickListener { searchView.isIconified = false }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()  // to prevent double trigger with hardware keyboard
                viewModel.searchSubmit(query, viewPager.currentItem == 2)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchTermChange(newText)
                return true
            }
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2) {
                    setTitle(R.string.title_workmates)
                    searchView.queryHint = getString(R.string.search_a_mate)
                } else {
                    setTitle(R.string.title_hungry)
                    searchView.queryHint = getString(R.string.search_a_place)
                }
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

    private fun initAuthorization(authorizationRequest: AuthorizationRequest) {
        Log.d(this.javaClass.canonicalName, "initAuthorization()")
        AuthorizationService(this).performAuthorizationRequest(
            authorizationRequest,
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, this::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            ),
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, this::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(LAST_VIEW_PAGER_ITEM, viewPager.currentItem)
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
        viewModel.onDestroy()
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

    override fun launch(osmId: Long) {
        startActivity(DetailsActivity.navigate(this, osmId))
    }
}


