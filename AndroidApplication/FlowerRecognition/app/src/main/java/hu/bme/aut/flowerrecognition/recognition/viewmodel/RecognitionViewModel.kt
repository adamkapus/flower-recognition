package hu.bme.aut.flowerrecognition.recognition.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.flowerrecognition.FlowerRecognitionApplication


class RecognitionViewModel : ViewModel() {
    private val analysisRounds = 10 //hány kör predikciót használjunk
    private  val maxDisplayedRecognition = 5

    private val _stateofrecognition = MutableLiveData(StateOfRecognition.READY_TO_START)
    val stateOfRecognition: LiveData<StateOfRecognition> = _stateofrecognition

    private var numberOfAnalysisRounds: Int = 0 //eddig hány predikció kör volt
    private val cumulatedRecognitions = mutableListOf<Recognition>() //a kumulált valószínőségek listája, tehát pl. ha első körben 0.9, másodikban 0.5, akkor 1.4
    private val _recognitionList = MutableLiveData<List<Recognition>>()
    val recognitionList: LiveData<List<Recognition>> = _recognitionList

    private val flowerLocRepo = FlowerRecognitionApplication.flowerLocationRepository

    //egy predikció lefutása után hívódik meg, paraméterül kapja a predikciót
    fun addRecognition(newRecognitions: List<Recognition>) {
        numberOfAnalysisRounds++

        if (numberOfAnalysisRounds >= analysisRounds) {
            finishRecognition()
        }

        newRecognitions.forEach { newRec ->
            val el = cumulatedRecognitions.find { it.label == newRec.label };
            if (el == null) {
                cumulatedRecognitions.add(newRec)
            } else {
                val idx = cumulatedRecognitions.indexOf(el); cumulatedRecognitions[idx] =
                    Recognition(label = el.label, confidence = el.confidence + newRec.confidence)
            }
        }

        postRecognitionList(cumulatedRecognitions.map { Rec ->
            Recognition(
                Rec.label,
                Rec.confidence / numberOfAnalysisRounds
            )
        }.toMutableList())

    }

    //Recognition list frissítése a top elemekkel
    private fun postRecognitionList(recognitions: MutableList<Recognition>) {
        val orderedAndTrimmedList = recognitions.sortedByDescending { it.confidence }.take(
            maxDisplayedRecognition
        )
        _recognitionList.postValue(orderedAndTrimmedList)
    }

    //Felismerés indítása
    fun startRecognition() {
        numberOfAnalysisRounds = 0
        cumulatedRecognitions.clear()
        _stateofrecognition.postValue(StateOfRecognition.IN_PROGRESS)
    }

    //felismerés leállítása
    private fun finishRecognition() {
        _stateofrecognition.postValue(StateOfRecognition.FINISHED)
    }

    //Virág beküldése a repository segítségével
    fun submitFlower(Lat: Double, Lng: Double, imageURL: String?) {
        val flowerName = cumulatedRecognitions.sortedByDescending { it.confidence }[0].label
        flowerLocRepo.addFlower(flowerName, Lat, Lng, imageURL)
        _stateofrecognition.postValue(StateOfRecognition.READY_TO_START)
    }

}

//Egy felismerés - címke, valószínűség és százalékos forma
data class Recognition(var label: String, var confidence: Float) {

    override fun toString(): String {
        return "$label / $probabilityString"
    }

    val probabilityString = String.format("%.1f%%", confidence * 100.0f)

}

//A felismerés állapota
enum class StateOfRecognition {
    READY_TO_START, //készen állunk egy új felismerésre
    IN_PROGRESS, //folyamatban van egy felismerés
    FINISHED //befejződött egy felismerés
}