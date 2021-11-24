package com.cleanup.go4lunch.ui.mates

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
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

        private val text = ContextCompat.getColor(itemView.context, R.color.colorOnSecondary)
        private val grey = ContextCompat.getColor(itemView.context, R.color.grey)

        fun bind(activityLauncher: DetailsActivityLauncher, viewState: MatesViewStateItem) {
            textView.text = viewState.text
            Glide.with(itemView).load(viewState.imageUrl)
                .apply(RequestOptions.circleCropTransform()).into(image)
            if (viewState.placeId != null) {
                itemView.setOnClickListener { activityLauncher.onClicked(viewState.placeId) }
                textView.setTextColor(text)
                // todo Nino : typeFace, c'est du Android.graphics : je mets ça au VM quand meme?
                textView.setTypeface(null, Typeface.NORMAL)
            } else {
                textView.setTextColor(grey)
                textView.setTypeface(null, Typeface.BOLD_ITALIC)
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
