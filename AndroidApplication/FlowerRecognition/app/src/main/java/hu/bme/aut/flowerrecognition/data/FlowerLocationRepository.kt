package hu.bme.aut.flowerrecognition.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import hu.bme.aut.flowerrecognition.data.model.FlowerLocation
import hu.bme.aut.flowerrecognition.util.FlowerResolver

//Repository osztály amely segítségével feltölthetünk egy új virágot, és lekérhetjük az eddigieket
class FlowerLocationRepository {
    companion object {
        private const val TAG = "FlowerLoc Repo"
    }

    private var flowerResolver = FlowerResolver() //label névből megjelenítési név feloldására

    private var db: FirebaseFirestore = Firebase.firestore

    //Egy FlowerLocation feltöltése a Firestoreba
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

    //lekérdezi az egész kollekciót, a hívó egy callback objektumot kell adjon, amin keresztül megtörténik majd az eredmény visszaadása
    fun refresh(callback: RefreshCallback) {
        db.collection("flowers").get().addOnSuccessListener { documents ->
            callback.onCompleted(documents.toObjects<FlowerLocation>())
        }.addOnFailureListener{
                _ -> callback.onError()}
    }

    interface RefreshCallback {
        fun onCompleted(flowers : List<FlowerLocation>)
        fun onError()
    }

}