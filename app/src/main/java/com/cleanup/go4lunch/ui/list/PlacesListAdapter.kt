package com.cleanup.go4lunch.ui.list

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
import com.cleanup.go4lunch.R


class PlacesListAdapter :
    ListAdapter<PlacesListViewState, PlacesListAdapter.ViewHolder>(PlacesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.list_item_name)
        private val distance: TextView = itemView.findViewById(R.id.list_item_distance)
        private val address: TextView = itemView.findViewById(R.id.list_item_address)
        private val colleagues: TextView = itemView.findViewById(R.id.list_item_colleagues)
        private val hours: TextView = itemView.findViewById(R.id.list_item_hours)
        private val likes: RatingBar = itemView.findViewById(R.id.list_item_likes)
        private val image: AppCompatImageView = itemView.findViewById(R.id.list_item_image)

        fun bind(viewState: PlacesListViewState) {
            name.text = viewState.name
            distance.text = viewState.distanceText
            address.text = viewState.address
            colleagues.text = viewState.colleagues
            hours.text = viewState.hours
            likes.rating = viewState.likes
            Glide.with(itemView).load(viewState.image).into(image)
        }
    }

    class PlacesDiffCallback : DiffUtil.ItemCallback<PlacesListViewState>() {
        override fun areItemsTheSame(
            oldItem: PlacesListViewState,
            newItem: PlacesListViewState
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PlacesListViewState,
            newItem: PlacesListViewState
        ): Boolean {
            return oldItem == newItem
        }
    }
}
