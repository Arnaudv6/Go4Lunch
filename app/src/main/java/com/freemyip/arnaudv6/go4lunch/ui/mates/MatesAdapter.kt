package com.freemyip.arnaudv6.go4lunch.ui.mates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.freemyip.arnaudv6.go4lunch.R

class MatesAdapter(private val onMateClicked: (Long?) -> Unit) :
    ListAdapter<MatesViewStateItem, MatesAdapter.ViewHolder>(MatesDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_mates_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(onMateClicked, getItem(position))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // could give itemView another name in constructor for clarity, or keep it, for clarity also
        private val image: AppCompatImageView = itemView.findViewById(R.id.mates_item_image)
        private val textView: AppCompatTextView = itemView.findViewById(R.id.mates_item_text)

        fun bind(onMateClicked: (Long?) -> Unit, viewState: MatesViewStateItem) {
            textView.text = viewState.text
            Glide.with(itemView).load(viewState.imageUrl)
                .apply(RequestOptions.circleCropTransform()).into(image)
            itemView.setOnClickListener { onMateClicked(viewState.placeId) }
            itemView.isEnabled = viewState.placeId != null
        }
    }

    class MatesDiffCallBack : DiffUtil.ItemCallback<MatesViewStateItem>() {
        override fun areItemsTheSame(oldItem: MatesViewStateItem, newItem: MatesViewStateItem) =
            oldItem.mateId == newItem.mateId

        override fun areContentsTheSame(oldItem: MatesViewStateItem, newItem: MatesViewStateItem) =
            oldItem == newItem
    }
}
