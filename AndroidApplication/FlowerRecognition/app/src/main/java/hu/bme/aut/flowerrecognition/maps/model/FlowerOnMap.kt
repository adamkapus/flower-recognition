package hu.bme.aut.flowerrecognition.maps.model

import hu.bme.aut.flowerrecognition.util.Rarity

data class FlowerOnMap(
    val name: String? = null,
    val Lat: Float? = null,
    val Lng: Float? = null,
    val imageUrl: String? = null,
    val displayName: String? = null,
    val rarity: Rarity? = null
)
