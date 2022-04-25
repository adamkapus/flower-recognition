package hu.bme.aut.flowerrecognition.maps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import hu.bme.aut.flowerrecognition.data.FlowerLocationRepository
import hu.bme.aut.flowerrecognition.data.model.FlowerLocation

class MapsViewModel : ViewModel() {

    ///private val _viewAbleFlowers = MutableLiveData<List<FlowerLocation>>()
    //val viewAbleFlowers : LiveData<List<FlowerLocation>> = _viewAbleFlowers

    private val flowerLocRepo = FlowerLocationRepository()

    fun refresh(){
        flowerLocRepo.refresh()
    }

    fun getFlowersLiveData(): LiveData<List<FlowerLocation>> {
        return flowerLocRepo.flowers
    }
}