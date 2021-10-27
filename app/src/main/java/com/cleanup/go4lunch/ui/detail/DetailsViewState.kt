package com.cleanup.go4lunch.ui.detail

data class DetailsViewState(
    val name: String,
    val goAtNoon: Boolean,
    val likes: Int,
    val address: String,
    val bigImageUrl: String,

    val call: String,
    val callActive: Boolean,
    val likeActive: Boolean,
    val website: String,
    val websiteActive: Boolean,

    val neighbourList: List<Neighbour>
) {
    data class Neighbour(
        val imageUrl: String,
        val text: String
    )
}