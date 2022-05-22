package hu.bme.aut.flowerrecognition.util

import hu.bme.aut.flowerrecognition.R

class FlowerResolver {
    private var flowerDisplayNameMap = HashMap<String, Int>()
    private var flowerRarityMap = HashMap<String, Rarity>()

    init {
        flowerDisplayNameMap["bluebell"] = R.string.flower_name_bluebell
        flowerRarityMap["bluebell"] = Rarity.COMMON

        flowerDisplayNameMap["buttercup"] = R.string.flower_name_buttercup
        flowerRarityMap["buttercup"] = Rarity.COMMON

        flowerDisplayNameMap["colts foot"] = R.string.flower_name_colts_foot
        flowerRarityMap["colts foot"] = Rarity.COMMON

        flowerDisplayNameMap["cowslip"] = R.string.flower_name_cowslip
        flowerRarityMap["cowslip"] = Rarity.RARE

        flowerDisplayNameMap["crocus"] = R.string.flower_name_crocus
        flowerRarityMap["crocus"] = Rarity.SUPER_RARE

        flowerDisplayNameMap["daffodil"] = R.string.flower_name_daffodil
        flowerRarityMap["daffodil"] = Rarity.COMMON

        flowerDisplayNameMap["daisy"] = R.string.flower_name_daisy
        flowerRarityMap["daisy"] = Rarity.COMMON

        flowerDisplayNameMap["dandelion"] = R.string.flower_name_dandelion
        flowerRarityMap["dandelion"] = Rarity.COMMON

        flowerDisplayNameMap["fritillary"] = R.string.flower_name_fritillary
        flowerRarityMap["fritillary"] = Rarity.SUPER_RARE

        flowerDisplayNameMap["iris"] = R.string.flower_name_iris
        flowerRarityMap["iris"] = Rarity.RARE

        flowerDisplayNameMap["lily valley"] = R.string.flower_name_lily_valley
        flowerRarityMap["lily valley"] = Rarity.RARE

        flowerDisplayNameMap["pansy"] = R.string.flower_name_pansy
        flowerRarityMap["pansy"] = Rarity.COMMON

        flowerDisplayNameMap["snowdrop"] = R.string.flower_name_snowdrop
        flowerRarityMap["snowdrop"] = Rarity.SUPER_RARE

        flowerDisplayNameMap["sunflower"] = R.string.flower_name_sunflower
        flowerRarityMap["sunflower"] = Rarity.RARE

        flowerDisplayNameMap["tiger lily"] = R.string.flower_name_tiger_lily
        flowerRarityMap["tiger lily"] = Rarity.SUPER_RARE


        flowerDisplayNameMap["tulip"] = R.string.flower_name_tulip
        flowerRarityMap["tulip"] = Rarity.RARE

        flowerDisplayNameMap["windflower"] = R.string.flower_name_windflower
        flowerRarityMap["windflower"] = Rarity.RARE

    }

    fun getDisplayName(label: String?): Int {
        return flowerDisplayNameMap[label] ?: R.string.flower_name_default
    }

    fun getRarity(label: String?): Rarity {
        return flowerRarityMap[label] ?: Rarity.COMMON
    }


}