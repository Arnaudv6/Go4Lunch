package com.cleanup.go4lunch.ui.mates

import android.text.Html
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
import com.cleanup.go4lunch.ui.main.DetailsActivityLauncher

class MatesAdapter(private val activityLauncher: DetailsActivityLauncher) :
    ListAdapter<MatesViewStateItem, MatesAdapter.ViewHolder>(MatesDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_mates_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(activityLauncher, getItem(position))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: AppCompatImageView = itemView.findViewById(R.id.mates_item_image)
        private val textView: TextView = itemView.findViewById(R.id.mates_item_text)

        fun bind(activityLauncher: DetailsActivityLauncher, viewState: MatesViewStateItem) {
            @Suppress("DEPRECATION") // Html.fromHtml(viewState.text, Html.FROM_HTML_MODE_LEGACY) in API24+
            textView.text = Html.fromHtml(viewState.text)
            Glide.with(itemView).load(viewState.imageUrl)
                .apply(RequestOptions.circleCropTransform()).into(image)
            viewState.placeId?.let {
                itemView.setOnClickListener { activityLauncher.onClicked(viewState.placeId) }
            }
        }
    }

    class MatesDiffCallBack : DiffUtil.ItemCallback<MatesViewStateItem>() {
        override fun areItemsTheSame(oldItem: MatesViewStateItem, newItem: MatesViewStateItem) =
            oldItem.mateId == newItem.mateId

        override fun areContentsTheSame(oldItem: MatesViewStateItem, newItem: MatesViewStateItem) =
            oldItem == newItem
    }
}
