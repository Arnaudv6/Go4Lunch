package com.cleanup.go4lunch.ui.mates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cleanup.go4lunch.R

class MatesAdapter : ListAdapter<MatesViewState, MatesAdapter.ViewHolder>(MatesDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_mates_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: AppCompatImageView = itemView.findViewById(R.id.mates_item_image)
        private val textView: TextView = itemView.findViewById(R.id.mates_item_text)

        fun bind(viewState: MatesViewState) {
            textView.text = viewState.text
            Glide.with(itemView).load(viewState.imageUrl).into(image)
        }
    }

    class MatesDiffCallBack : DiffUtil.ItemCallback<MatesViewState>() {
        override fun areItemsTheSame(oldItem: MatesViewState, newItem: MatesViewState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MatesViewState, newItem: MatesViewState): Boolean {
            return oldItem == newItem
        }

    }

}

