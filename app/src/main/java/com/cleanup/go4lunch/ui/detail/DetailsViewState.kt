package com.cleanup.go4lunch.ui.detail

import androidx.annotation.ColorInt

data class DetailsViewState(
    val name: String,
    @ColorInt
    val goAtNoonColor: Int,
    val rating: Float?,
    val address: String,
    val bigImageUrl: String,

    @ColorInt
    val callColor: Int,
    val callActive: Boolean,
    @ColorInt
    val likeColor: Int,
    val likeActive: Boolean,
    @ColorInt
    val websiteColor: Int,
    val websiteActive: Boolean,

    val colleaguesList: List<Item>
) {
    data class Item(
        val mateId: Long,
        val imageUrl: String,
        val text: String
    )
}
