package com.cleanup.go4lunch.ui.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cleanup.go4lunch.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {

    private val viewModel: DetailsViewModel by viewModels()

    companion object {

        const val OSM_ID = "osm_id"

        fun navigate(caller: Activity, osmId: Long): Intent =
            Intent(caller, DetailsActivity::class.java).apply { putExtra(OSM_ID, osmId) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_details)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val image: ImageView = findViewById(R.id.app_bar_image)
        val name: TextView = findViewById(R.id.details_name)
        val likes: RatingBar = findViewById(R.id.details_likes)
        val address: TextView = findViewById(R.id.details_address)
        val call: TextView = findViewById(R.id.details_call)
        val like: TextView = findViewById(R.id.details_like)
        val website: TextView = findViewById(R.id.details_website)
        val button: FloatingActionButton = findViewById(R.id.details_floatingActionButton)
        val recycler: RecyclerView = findViewById(R.id.details_recycler_view)

        val adapter = DetailsAdapter()
        recycler.adapter = adapter

        viewModel.viewStateLiveData.observe(this) {
            Glide.with(baseContext).load(it.bigImageUrl).into(image)
            name.text = it.name
            // todo Nino: ce if là ne peut que rester?
            if (it.rating != null) likes.rating = it.rating
            address.text = it.address

            call.compoundDrawablesRelative.filterNotNull()[0].setTint(it.callColor)
            call.setTextColor(it.callColor)
            // isClickable works not, setAllowClickWhenDisabled() is API 31+
            // Todo Nino: là faut que je pass le click au VM pour dégager le if?
            call.setOnClickListener { _ ->
                if (it.callActive) startActivity(
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.fromParts("tel", it.call, null)
                    )
                )
                // todo click sounds even when inactive. return false?
            }
            like.compoundDrawablesRelative.filterNotNull()[0].setTint(it.likeColor)
            like.setTextColor(it.likeColor)
            like.isClickable = it.likeActive
            website.compoundDrawablesRelative.filterNotNull()[0].setTint(it.websiteColor)
            website.setTextColor(it.websiteColor)
            website.isClickable = it.websiteActive
            website.setOnClickListener { _ ->
                if (it.websiteActive) startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(it.website)
                    )
                )
            }
            button.setColorFilter(it.goAtNoonColor)
            button.setOnClickListener {
                viewModel.goingAtNoonClicked()
            }

            adapter.submitList(it.colleaguesList)
        }

    }
}


