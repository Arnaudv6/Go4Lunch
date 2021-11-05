package com.cleanup.go4lunch.ui.detail

import android.app.Activity
import android.content.Intent
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

        private const val OSM_ID = "osm_id"

        fun navigate(caller: Activity, osmId: Long): Intent {
            val intent = Intent(caller, DetailsActivity::class.java)
            intent.putExtra(OSM_ID, osmId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_restaurant)

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

        viewModel.onCreate(intent.getLongExtra(OSM_ID, 0))
        viewModel.viewStateLiveData.observe(this) {
            Glide.with(baseContext).load(it.bigImageUrl).into(image)
            name.text = it.name
            if (it.rating != null) likes.rating = it.rating
            address.text = it.address
            call.isClickable = it.callActive
            call.setOnClickListener {
                // intent on it.call
            }
            like.isClickable = it.likeActive
            website.isClickable = it.websiteActive
            website.setOnClickListener {
                // intent on it.website
            }
            button.isClickable = it.goAtNoon

            adapter.submitList(it.neighbourList)
        }

    }
}


