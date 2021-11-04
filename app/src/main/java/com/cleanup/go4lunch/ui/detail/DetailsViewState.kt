package com.cleanup.go4lunch.ui.detail

data class DetailsViewState(
    val name: String,
    val goAtNoon: Boolean,
    val rating: Float?,
    val address: String,
    val bigImageUrl: String,

    val call: String?, // todo why
    val callActive: Boolean,
    val likeActive: Boolean,
    val website: String,
    val websiteActive: Boolean,

    val neighbourList: List<Item>
) {
    data class Item(
        val mateId: Long,
        val imageUrl: String,
        val text: String
    )
}
