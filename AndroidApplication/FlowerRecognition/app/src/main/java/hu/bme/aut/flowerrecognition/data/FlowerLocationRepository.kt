package hu.bme.aut.flowerrecognition.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import hu.bme.aut.flowerrecognition.data.model.FlowerLocation
import hu.bme.aut.flowerrecognition.util.FlowerResolver

private const val TAG = "FlowrLoc Repo"

class FlowerLocationRepository {

    private var flowerResolver = FlowerResolver()

    private var db: FirebaseFirestore = Firebase.firestore

    fun addFlower(name: String, Lat: Double, Lng: Double, imageURL: String?) {
        val data = hashMapOf(
            "name" to name,
            "Lat" to Lat,
            "Lng" to Lng,
            "imageUrl" to imageURL,
            "rarity" to flowerResolver.getRarity(name).toString()
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

    fun refresh(callback: RefreshCallback) {
        db.collection("flowers").get().addOnSuccessListener { documents ->
            callback.onCompleted(documents.toObjects<FlowerLocation>())
        }.addOnFailureListener{
                e -> callback.onError()}
    }

    interface RefreshCallback {
        fun onCompleted(flowers : List<FlowerLocation>)
        fun onError()
    }


}