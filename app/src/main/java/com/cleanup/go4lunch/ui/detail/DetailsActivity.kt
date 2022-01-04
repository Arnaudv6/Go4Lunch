package com.cleanup.go4lunch.ui.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cleanup.go4lunch.R
import com.cleanup.go4lunch.exhaustive
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {

    private val viewModel: DetailsViewModel by viewModels()

    companion object {
        const val OSM_ID = "osm_id"

        fun navigate(context: Context, osmId: Long): Intent =
            Intent(context, DetailsActivity::class.java).apply { putExtra(OSM_ID, osmId) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_details)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        findViewById<AppCompatImageButton>(R.id.back_home_button).setOnClickListener {
            // setDisplayHomeAsUpEnabled() could work with
            //  https://stackoverflow.com/questions/28438030
            //  but then we also need the contrasted shadow
            onBackPressed()
        }

        val image: AppCompatImageView = findViewById(R.id.app_bar_image)
        val name: AppCompatTextView = findViewById(R.id.details_name)
        val likes: AppCompatRatingBar = findViewById(R.id.details_likes)
        val address: AppCompatTextView = findViewById(R.id.details_address)
        val call: AppCompatTextView = findViewById(R.id.details_call)
        val like: AppCompatTextView = findViewById(R.id.details_like)
        val website: AppCompatTextView = findViewById(R.id.details_website)
        val button: FloatingActionButton = findViewById(R.id.details_floatingActionButton)
        val recycler: RecyclerView = findViewById(R.id.details_recycler_view)

        val adapter = DetailsAdapter()
        recycler.adapter = adapter

        viewModel.viewStateLiveData.observe(this) {
            Glide.with(baseContext).load(it.bigImageUrl).into(image)
            name.text = it.name
            it.rating?.let { float ->
                // parallax + multiplier would achieve a nice effect, but be tricky on device ddp
                likes.visibility = View.VISIBLE
                likes.rating = float
            }
            address.text = it.address

            call.isEnabled = it.callActive
            // isClickable works not, setAllowClickWhenDisabled() is API 31+
            call.compoundDrawablesRelative.filterNotNull()[0].setTint(it.callColor)
            call.setTextColor(it.callColor)
            call.setOnClickListener { viewModel.callClicked() }

            like.compoundDrawablesRelative.filterNotNull()[0].setTint(it.likeColor)
            like.setTextColor(it.likeColor)
            like.setOnClickListener { viewModel.likeClicked() }

            website.isEnabled = it.websiteActive
            website.compoundDrawablesRelative.filterNotNull()[0].setTint(it.websiteColor)
            website.setTextColor(it.websiteColor)
            website.setOnClickListener { viewModel.webClicked() }

            button.setColorFilter(it.goAtNoonColor)
            button.setOnClickListener { viewModel.goingAtNoonClicked() }

            findViewById<AppBarLayout>(R.id.appbar).addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                    val ratio = verticalOffset / appBarLayout.totalScrollRange.toFloat()
                    if (ratio < -0.7) button.hide() else button.show()

                    /*
                    if (verticalOffset == 0) return@OnOffsetChangedListener
                    (button.layoutParams as CoordinatorLayout.LayoutParams).anchorId =
                        if (verticalOffset < -300) R.id.collapsing
                        else R.id.details_recycler_view

                     */
                })
            adapter.submitList(it.colleaguesList)
        }

        viewModel.intentSingleLiveEvent.observe(this) {
            startActivity(
                when (it) {
                    is DetailsViewAction.Call -> Intent(
                        Intent.ACTION_DIAL,
                        Uri.fromParts("tel", it.number, null)
                    )
                    is DetailsViewAction.Surf -> Intent(Intent.ACTION_VIEW, Uri.parse(it.address))
                }.exhaustive
            )
        }

    }
}


