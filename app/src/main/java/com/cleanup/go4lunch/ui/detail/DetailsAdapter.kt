package com.cleanup.go4lunch.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cleanup.go4lunch.R

class DetailsAdapter :
    ListAdapter<DetailsViewState.Item, DetailsAdapter.ViewHolder>(ItemDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_mates_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: AppCompatImageView = itemView.findViewById(R.id.mates_item_image)
        private val textView: TextView = itemView.findViewById(R.id.mates_item_text)

        fun bind(viewState: DetailsViewState.Item) {
            textView.text = viewState.text
            Glide.with(itemView).load(viewState.imageUrl)
                .apply(RequestOptions.circleCropTransform()).into(image)
        }
    }

    class ItemDiffCallBack : DiffUtil.ItemCallback<DetailsViewState.Item>() {
        override fun areItemsTheSame(oldItem: DetailsViewState.Item, newItem: DetailsViewState.Item) =
            oldItem.mateId == newItem.mateId

        override fun areContentsTheSame(oldItem: DetailsViewState.Item, newItem: DetailsViewState.Item) =
            oldItem == newItem
    }
}

