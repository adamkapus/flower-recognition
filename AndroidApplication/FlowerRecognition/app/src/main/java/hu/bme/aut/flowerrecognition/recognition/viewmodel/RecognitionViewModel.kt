package hu.bme.aut.flowerrecognition.recognition.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import hu.bme.aut.flowerrecognition.FlowerRecognitionApplication
import hu.bme.aut.flowerrecognition.data.FlowerLocationRepository

private const val MAX_ANALYSIS_ROUNDS = 10
private const val MAX_DISPLAYED_RECOGNITIONS = 5

class RecognitionViewModel : ViewModel() {

    private val _stateofrecognition = MutableLiveData(StateOfRecognition.READY_TO_START)
    val stateOfRecognition: LiveData<StateOfRecognition> = _stateofrecognition

    private var numberOfAnalysisRounds : Int = 0
    private val cumulatedRecognitions  = mutableListOf<Recognition>()
    private val _recognitionList = MutableLiveData<List<Recognition>>()
    val recognitionList: LiveData<List<Recognition>> = _recognitionList

    private val flowerLocRepo = FlowerRecognitionApplication.flowerLocationRepository

    fun addRecognition(newRecognitions: List<Recognition>){
        numberOfAnalysisRounds++
        if(numberOfAnalysisRounds >= MAX_ANALYSIS_ROUNDS){
            Log.d("ViewModel", "Meg kéne állni")
            finishRecognition()
        }

        newRecognitions.forEach {
            newRec ->
            val el = cumulatedRecognitions.find { it.label == newRec.label };
            if(el == null) {cumulatedRecognitions.add(newRec)}
            else{ val idx = cumulatedRecognitions.indexOf(el); cumulatedRecognitions[idx] =
                Recognition(label=el.label, confidence = el.confidence + newRec.confidence)
            }
        }

        postRecognitionList(cumulatedRecognitions.map { Rec -> Recognition(Rec.label,Rec.confidence /numberOfAnalysisRounds) }.toMutableList())

    }

    private fun postRecognitionList(recognitions: MutableList<Recognition>){
        val orderedAndTrimmedList = recognitions.sortedByDescending { it.confidence }.take(
            MAX_DISPLAYED_RECOGNITIONS
        )
        _recognitionList.postValue(orderedAndTrimmedList)
    }

    fun startRecognition(){
        numberOfAnalysisRounds =0
        cumulatedRecognitions.clear()
        _stateofrecognition.postValue(StateOfRecognition.IN_PROGRESS)
    }

    private fun finishRecognition(){
        _stateofrecognition.postValue(StateOfRecognition.FINISHED)
    }

    fun submitFlower(Lat: Double, Lng: Double){
        val flowerName = cumulatedRecognitions.sortedByDescending { it.confidence }[0].label
        flowerLocRepo.addFlower(flowerName, Lat, Lng)
        _stateofrecognition.postValue(StateOfRecognition.READY_TO_START)
    }

}


data class Recognition(var label:String, var confidence:Float) {

    override fun toString():String{
        return "$label / $probabilityString"
    }

    val probabilityString = String.format("%.1f%%", confidence * 100.0f)

}

enum class StateOfRecognition{
    READY_TO_START,
    IN_PROGRESS,
    FINISHED
}