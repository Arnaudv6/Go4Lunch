package com.freemyip.go4lunch.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freemyip.go4lunch.R
import com.freemyip.go4lunch.ui.main.DetailsActivityLauncher

class PlacesListAdapter(private val activityLauncher: DetailsActivityLauncher) :
    ListAdapter<PlacesListViewState, PlacesListAdapter.ViewHolder>(PlacesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_list_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(activityLauncher, it) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.list_item_name)
        private val distance: TextView = itemView.findViewById(R.id.list_item_distance)
        private val address: TextView = itemView.findViewById(R.id.list_item_address)
        private val mates: TextView = itemView.findViewById(R.id.list_item_mates)
        private val hours: TextView = itemView.findViewById(R.id.list_item_hours)
        private val likes: RatingBar = itemView.findViewById(R.id.list_item_likes)
        private val image: AppCompatImageView = itemView.findViewById(R.id.list_item_image)

        fun bind(activityLauncher: DetailsActivityLauncher, viewState: PlacesListViewState) {
            itemView.setOnClickListener { activityLauncher.launch(viewState.id) }
            name.text = viewState.name
            distance.text = viewState.distanceText
            address.text = viewState.address
            mates.text = viewState.mates
            hours.text = viewState.hours
            hours.setTextColor(viewState.hoursColor)
            likes.visibility = View.GONE
            viewState.rating?.let{
                likes.visibility = View.VISIBLE
                likes.rating = it
            }
            Glide.with(itemView).load(viewState.image).into(image)
        }
    }

    class PlacesDiffCallback : DiffUtil.ItemCallback<PlacesListViewState>() {
        override fun areItemsTheSame(
            oldItem: PlacesListViewState,
            newItem: PlacesListViewState
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PlacesListViewState,
            newItem: PlacesListViewState
        ): Boolean = oldItem == newItem
    }
}
