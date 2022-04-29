package hu.bme.aut.flowerrecognition.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import hu.bme.aut.flowerrecognition.data.model.FlowerLocation

private const val TAG = "FlowrLoc Repo"

class FlowerLocationRepository {

    private var db: FirebaseFirestore = Firebase.firestore

    private val _flowers = MutableLiveData<List<FlowerLocation>>()
    val flowers: LiveData<List<FlowerLocation>> = _flowers


    fun addFlower(name: String, Lat: Double, Lng: Double) {
        val data = hashMapOf(
            "name" to name,
            "Lat" to Lat,
            "Lng" to Lng
        )
        db.collection("flowers")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    fun refresh() {
        db.collection("flowers").get().addOnSuccessListener { documents ->
            _flowers.postValue(documents.toObjects<FlowerLocation>())
        }
    }


}