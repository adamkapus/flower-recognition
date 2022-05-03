package hu.bme.aut.flowerrecognition.util

import hu.bme.aut.flowerrecognition.R

class RarityResolver {
    private var rarityMap = HashMap<Rarity, Int>()

    init {
        rarityMap[Rarity.COMMON] = R.string.rarity_common
        rarityMap[Rarity.RARE] = R.string.rarity_rare
        rarityMap[Rarity.SUPER_RARE] = R.string.rarity_super_rare
    }

    fun getDisplayName(rarity: Rarity?): Int {
        return rarityMap[rarity] ?: R.string.rarity_default
    }


}

enum class Rarity {
    COMMON,
    RARE,
    SUPER_RARE
}