package hu.bme.aut.flowerrecognition.data.model

data class FlowerLocation(
    val name: String? = null, //label név
    val Lat: Float? = null, //pozíció szélességi foka
    val Lng: Float? = null, //pozíció hosszúsági foka
    val imageUrl: String? = null, //Url a virág képére
    val rarity: String? = null //virág ritkasága
)
