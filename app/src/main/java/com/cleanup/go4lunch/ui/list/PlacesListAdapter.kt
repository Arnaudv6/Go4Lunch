package com.cleanup.go4lunch.ui.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class PlacesListAdapter: ListAdapter<PlacesListViewState, PlacesListAdapter.ViewHolder>(PlacesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    class PlacesDiffCallback: DiffUtil.ItemCallback<PlacesListViewState>() {
        override fun areItemsTheSame(
            oldItem: PlacesListViewState,
            newItem: PlacesListViewState
        ): Boolean {
            TODO("Not yet implemented")
        }

        override fun areContentsTheSame(
            oldItem: PlacesListViewState,
            newItem: PlacesListViewState
        ): Boolean {
            TODO("Not yet implemented")
        }

    }

}