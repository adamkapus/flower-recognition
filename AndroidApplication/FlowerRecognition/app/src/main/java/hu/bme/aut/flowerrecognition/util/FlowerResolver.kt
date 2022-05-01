package hu.bme.aut.flowerrecognition.util

import hu.bme.aut.flowerrecognition.R

class FlowerResolver {
    private var flowerDisplayNameMap = HashMap<String, Int>()

    init {
        flowerDisplayNameMap["snowdrop"] = R.string.flower_name_snowdrop
        flowerDisplayNameMap["sunflower"] = R.string.flower_name_sunflower
    }

    fun getDisplayName(label: String?): Int {
        return flowerDisplayNameMap[label] ?: R.string.flower_name_default
    }


}