package hu.bme.aut.flowerrecognition.maps.model

import hu.bme.aut.flowerrecognition.util.Rarity

data class FlowerOnMap(
    val name: String? = null, //label név
    val Lat: Float? = null, //pozíció szélességi foka
    val Lng: Float? = null, //pozíció hosszúsági foka
    val imageUrl: String? = null, //Url a virág képére
    val rarity: Rarity? = null, //virág ritkasága
    val displayName: String? = null, //a megjelenítés során használt név
)
