package com.cleanup.go4lunch.ui.detail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cleanup.go4lunch.R

class DetailsActivity : AppCompatActivity() {

    companion object {
        private const val OSM_ID = "osm_id"
        fun navigate(caller: Activity, osmId: Long): Intent {
            val intent = Intent(caller, this::class.java)
            intent.putExtra(OSM_ID, osmId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        setContentView(R.layout.activity_restaurant)



    }

}