package com.cleanup.go4lunch.ui.detail

import com.cleanup.go4lunch.ui.mates.MatesViewStateItem
data class DetailsViewState(
    val name: String,
    val goAtNoon: Boolean,
    val likes: Int,
    val address: String,
    val bigImageUrl: String,

    val call: String?, // todo why
    val callActive: Boolean,
    val likeActive: Boolean,
    val website: String,
    val websiteActive: Boolean,

    val neighbourList: List<MatesViewStateItem>
)
