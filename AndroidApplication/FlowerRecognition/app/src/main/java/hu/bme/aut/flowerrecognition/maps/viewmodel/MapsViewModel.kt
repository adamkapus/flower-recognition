package hu.bme.aut.flowerrecognition.maps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import hu.bme.aut.flowerrecognition.FlowerRecognitionApplication
import hu.bme.aut.flowerrecognition.data.FlowerLocationRepository
import hu.bme.aut.flowerrecognition.data.model.FlowerLocation
import hu.bme.aut.flowerrecognition.util.Rarity

class MapsViewModel : ViewModel() {

    ///private val _viewAbleFlowers = MutableLiveData<List<FlowerLocation>>()
    //val viewAbleFlowers : LiveData<List<FlowerLocation>> = _viewAbleFlowers

    private val flowerLocRepo = FlowerRecognitionApplication.flowerLocationRepository

    private val raritySet = mutableSetOf<Rarity>(*Rarity.values());
    private val _viewableRarities = MutableLiveData<Set<Rarity>>()
    val viewableRarities: LiveData<Set<Rarity>> = _viewableRarities

    fun refresh() {
        flowerLocRepo.refresh()
    }

    fun getFlowersLiveData(): LiveData<List<FlowerLocation>> {
        return flowerLocRepo.flowers
    }

    fun modifyRarities(rarity: Rarity, isChecked: Boolean) {
        if (isChecked) {
            raritySet.add(rarity)
        } else {
            raritySet.remove(rarity)
        }

        _viewableRarities.postValue(raritySet)
    }
}