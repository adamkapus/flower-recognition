package hu.bme.aut.flowerrecognition.maps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.flowerrecognition.FlowerRecognitionApplication
import hu.bme.aut.flowerrecognition.data.FlowerLocationRepository
import hu.bme.aut.flowerrecognition.data.model.FlowerLocation
import hu.bme.aut.flowerrecognition.util.Rarity

class MapsViewModel : ViewModel() {

    private val flowerLocRepo = FlowerRecognitionApplication.flowerLocationRepository

    private val raritySet = mutableSetOf<Rarity>(*Rarity.values()); //az összes ritkasági kategória
    private val _viewableRarities = MutableLiveData<Set<Rarity>>()
    val viewableRarities: LiveData<Set<Rarity>> = _viewableRarities //UI-on látható ritkaságok

    private val _flowers = MutableLiveData<List<FlowerLocation>>()
    val flowers: LiveData<List<FlowerLocation>> = _flowers //FlowerLocation-ok listája

    //újratölti a virágok listáját
    fun refresh() {
        flowerLocRepo.refresh(object : FlowerLocationRepository.RefreshCallback {
            override fun onCompleted(flowers: List<FlowerLocation>) {
                _flowers.postValue(flowers)
            }

            override fun onError() {

            }
        })
    }

    //módosítja a látható ritkaságok listáját, paraméterben megkapja, hogy melyik ritkaságot kell kivenni, vagy hozzáadni
    fun modifyRarities(rarity: Rarity, isChecked: Boolean) {
        if (isChecked) {
            raritySet.add(rarity)
        } else {
            raritySet.remove(rarity)
        }

        _viewableRarities.postValue(raritySet)
    }
}