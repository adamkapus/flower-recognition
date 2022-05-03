package hu.bme.aut.flowerrecognition.util

import hu.bme.aut.flowerrecognition.R

class FlowerResolver {
    private var flowerDisplayNameMap = HashMap<String, Int>()
    private var flowerRarityMap = HashMap<String, Rarity>()

    init {
        flowerDisplayNameMap["snowdrop"] = R.string.flower_name_snowdrop
        flowerRarityMap["snowdrop"] = Rarity.SUPER_RARE

        flowerDisplayNameMap["sunflower"] = R.string.flower_name_sunflower
        flowerRarityMap["sunflower"] = Rarity.RARE

        flowerDisplayNameMap["dandelion"] = R.string.flower_name_dandelion
        flowerRarityMap["dandelion"] = Rarity.COMMON

        flowerDisplayNameMap["tulip"] = R.string.flower_name_tulip
        flowerRarityMap["tulip"] = Rarity.RARE

        flowerDisplayNameMap["pansy"] = R.string.flower_name_pansy
        flowerRarityMap["pansy"] = Rarity.COMMON

        flowerDisplayNameMap["iris"] = R.string.flower_name_iris
        flowerRarityMap["iris"] = Rarity.RARE

        flowerDisplayNameMap["daisy"] = R.string.flower_name_daisy
        flowerRarityMap["daisy"] = Rarity.COMMON
    }

    fun getDisplayName(label: String?): Int {
        return flowerDisplayNameMap[label] ?: R.string.flower_name_default
    }

    fun getRarity(label: String?): Rarity {
        return flowerRarityMap[label] ?: Rarity.COMMON
    }


}