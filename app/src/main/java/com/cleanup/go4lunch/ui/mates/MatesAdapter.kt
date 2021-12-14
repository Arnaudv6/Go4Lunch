package com.cleanup.go4lunch.ui.mates

import android.text.Html
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
import com.cleanup.go4lunch.R

// todo Nino: in listAdapter, I pass activity's listener as argument, here I pass the viewModel. VM?
class MatesAdapter(private val viewModel: MatesViewModel) :
    ListAdapter<MatesViewStateItem, MatesAdapter.ViewHolder>(MatesDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_mates_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(viewModel, getItem(position))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // could give itemView another name in constructor for clarity, or keep it, for clarity also
        private val image: AppCompatImageView = itemView.findViewById(R.id.mates_item_image)
        private val textView: AppCompatTextView = itemView.findViewById(R.id.mates_item_text)

        fun bind(viewModel: MatesViewModel, viewState: MatesViewStateItem) {
            @Suppress("DEPRECATION") // Html.FROM_HTML_MODE_LEGACY is API24+
            textView.text = Html.fromHtml(viewState.text)  // https://stackoverflow.com/a/2938184
            // should there be more than just this span in the project, consider using spannable-ktx
            // W/View: requestLayout() improperly called by androidx.appcompat.widget.AppCompatTextView{43a4f78 V.ED..... ......ID 189,38-465,109} during layout: running second layout pass
            Glide.with(itemView).load(viewState.imageUrl)
                .apply(RequestOptions.circleCropTransform()).into(image)
            itemView.setOnClickListener { viewModel.mateClicked(viewState.mateId) }
        }
    }

    class MatesDiffCallBack : DiffUtil.ItemCallback<MatesViewStateItem>() {
        override fun areItemsTheSame(oldItem: MatesViewStateItem, newItem: MatesViewStateItem) =
            oldItem.mateId == newItem.mateId

        override fun areContentsTheSame(oldItem: MatesViewStateItem, newItem: MatesViewStateItem) =
            oldItem == newItem
    }
}
